package es.upm.amusementpark.persistence.file;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.ParkFormat;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.persistence.PersistenceException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and retrieves every ticket. All ticket types share one identifier space, which includes the identifiers
 * of the individual tickets composing group tickets.
 *
 * <p>File format, one record per line:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;&lt;user&gt;
 * GROUP;id;date;groupId (empty if the group was deleted);basePrice;discount
 * MEMBER;groupTicketId;&lt;user&gt;            (one per composed ticket, after its GROUP line)
 * &lt;user&gt; = ACTIVE|FORMER;dni;firstName;surnames;email
 * </pre>
 * ACTIVE users are linked to the registered user with that DNI; FORMER (deleted) users are rebuilt from the
 * snapshot so historical tickets survive.
 */
public class FileTicketRepository implements TicketRepository {

    public static final String FILE_NAME = "tickets.txt";

    private static final String ACTIVE = "ACTIVE";
    private static final String FORMER = "FORMER";

    private final Path file;
    private final UserRepository users;
    private final GroupRepository groups;
    private final List<Ticket> tickets = new ArrayList<>();

    public FileTicketRepository(Path file, UserRepository users, GroupRepository groups) {
        this.file = file;
        this.users = users;
        this.groups = groups;
    }

    public void load() {
        tickets.clear();
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
            parse(lines);
        } catch (IOException e) {
            throw new PersistenceException("Cannot read tickets file " + file, e);
        }
    }

    private void parse(List<String> lines) {
        int i = 0;
        while (i < lines.size()) {
            String[] fields = lines.get(i).split(";", -1);
            i++;
            try {
                if (fields[0].equals("INDIVIDUAL") && fields.length == 10) {
                    tickets.add(new IndividualTicket(fields[1], ParkFormat.parseDate(fields[2]),
                            readUser(fields, 5), new BigDecimal(fields[3]), Integer.parseInt(fields[4])));
                } else if (fields[0].equals("GROUP") && fields.length == 6) {
                    String id = fields[1];
                    LocalDate date = ParkFormat.parseDate(fields[2]);
                    Group group = fields[3].isEmpty() ? null : groups.findById(fields[3]);
                    BigDecimal basePrice = new BigDecimal(fields[4]);
                    int discount = Integer.parseInt(fields[5]);
                    List<IndividualTicket> composition = new ArrayList<>();
                    while (i < lines.size() && lines.get(i).startsWith("MEMBER;" + id + ";")) {
                        String[] member = lines.get(i).split(";", -1);
                        i++;
                        User user = readUser(member, 2);
                        composition.add(new IndividualTicket(GroupTicket.composedId(id, user), date, user,
                                basePrice, discount));
                    }
                    tickets.add(new GroupTicket(id, date, group, composition));
                } else {
                    throw new IllegalArgumentException("unknown record");
                }
            } catch (RuntimeException e) {
                System.err.println("WARNING: skipping malformed ticket record '" + String.join(";", fields)
                        + "' in " + file);
            }
        }
    }

    private User readUser(String[] fields, int from) {
        String status = fields[from];
        String dni = fields[from + 1];
        if (status.equals(ACTIVE)) {
            User registered = users.findByDni(dni);
            if (registered != null) {
                return registered;
            }
        }
        return new User(fields[from + 2], fields[from + 3], dni, fields[from + 4], "", "", "");
    }

    @Override
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Ticket ticket : tickets) {
                if (ticket instanceof IndividualTicket individual) {
                    writer.write(String.join(";", "INDIVIDUAL", individual.getId(),
                            ParkFormat.formatDate(individual.getDate()), individual.getBasePrice().toPlainString(),
                            String.valueOf(individual.getDiscountPercent()), writeUser(individual.getUser())));
                    writer.newLine();
                } else if (ticket instanceof GroupTicket group) {
                    IndividualTicket first = group.getComposition().get(0);
                    writer.write(String.join(";", "GROUP", group.getId(), ParkFormat.formatDate(group.getDate()),
                            group.getGroup() == null ? "" : group.getGroup().getId(),
                            first.getBasePrice().toPlainString(), String.valueOf(first.getDiscountPercent())));
                    writer.newLine();
                    for (IndividualTicket member : group.getComposition()) {
                        writer.write("MEMBER;" + group.getId() + ";" + writeUser(member.getUser()));
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot write tickets file " + file, e);
        }
    }

    private String writeUser(User user) {
        return String.join(";", users.contains(user) ? ACTIVE : FORMER, user.getDni(), user.getFirstName(),
                user.getSurnames(), user.getEmail());
    }

    /**
     * Top-level tickets (individual tickets bought alone and group tickets).
     */
    @Override
    public List<Ticket> findAll() {
        return Collections.unmodifiableList(tickets);
    }

    /**
     * Finds any ticket by identifier, including individual tickets composing a group ticket.
     */
    @Override
    public Ticket findById(String id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
            if (ticket instanceof GroupTicket group) {
                for (IndividualTicket member : group.getComposition()) {
                    if (member.getId().equals(id)) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean exists(String id) {
        return findById(id) != null;
    }

    /**
     * Whether the ticket is part of the composition of a group ticket.
     */
    @Override
    public boolean isComposed(Ticket ticket) {
        for (Ticket candidate : tickets) {
            if (candidate instanceof GroupTicket group && group.getComposition().contains(ticket)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Every individual ticket (bought alone or composing a group ticket) held by the user.
     */
    @Override
    public List<IndividualTicket> ticketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            } else if (ticket instanceof GroupTicket group) {
                for (IndividualTicket member : group.getComposition()) {
                    if (member.getUser() == user) {
                        result.add(member);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    @Override
    public void add(Ticket ticket) {
        tickets.add(ticket);
        save();
    }

    @Override
    public void remove(Ticket ticket) {
        tickets.remove(ticket);
        save();
    }
}
