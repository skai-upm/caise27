package es.upm.amusementpark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores individual and group tickets in a text file. Record types:
 * <ul>
 *   <li>{@code I;id;date;price;dni;name;surname;email} - individual ticket</li>
 *   <li>{@code G;id;date;price;groupId;groupName} - group ticket, followed by</li>
 *   <li>{@code C;id;date;price;dni;name;surname;email} - one line per constituent ticket</li>
 * </ul>
 * Holder name, surname and email are kept so that historical tickets survive the
 * deletion of their user; a deleted holder or group is rebuilt from that data.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";
    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";
    private static final String CONSTITUENT = "C";
    private static final int TICKET_FIELDS = 8;
    private static final int GROUP_FIELDS = 6;

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        Map<String, User> holders = new HashMap<>(usersByDni);
        Map<String, Group> groups = new HashMap<>(groupsById);
        String[] pendingGroup = null;
        List<IndividualTicket> constituents = new ArrayList<>();
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(SEPARATOR, -1);
            try {
                if (CONSTITUENT.equals(parts[0]) && pendingGroup != null && parts.length == TICKET_FIELDS) {
                    constituents.add(individual(parts, holders));
                    continue;
                }
                if (pendingGroup != null) {
                    tickets.add(groupTicket(pendingGroup, constituents, groups));
                    pendingGroup = null;
                    constituents = new ArrayList<>();
                }
                if (INDIVIDUAL.equals(parts[0]) && parts.length == TICKET_FIELDS) {
                    tickets.add(individual(parts, holders));
                } else if (GROUP.equals(parts[0]) && parts.length == GROUP_FIELDS) {
                    pendingGroup = parts;
                } else {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                }
            } catch (DateTimeParseException | NumberFormatException e) {
                System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
            }
        }
        if (pendingGroup != null) {
            tickets.add(groupTicket(pendingGroup, constituents, groups));
        }
        return tickets;
    }

    public void save(List<Ticket> tickets) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Ticket ticket : tickets) {
                if (ticket instanceof GroupTicket groupTicket) {
                    writer.write(String.join(SEPARATOR, GROUP, groupTicket.getId(),
                            groupTicket.getDate().toString(), groupTicket.getPrice().toPlainString(),
                            groupTicket.getGroup().getId(), groupTicket.getGroup().getName()));
                    writer.newLine();
                    for (IndividualTicket constituent : groupTicket.getTickets()) {
                        writer.write(individualLine(CONSTITUENT, constituent));
                        writer.newLine();
                    }
                } else if (ticket instanceof IndividualTicket individual) {
                    writer.write(individualLine(INDIVIDUAL, individual));
                    writer.newLine();
                }
            }
        }
    }

    private static String individualLine(String type, IndividualTicket ticket) {
        User user = ticket.getUser();
        return String.join(SEPARATOR, type, ticket.getId(), ticket.getDate().toString(),
                ticket.getPrice().toPlainString(), user.getDni(), user.getName(), user.getSurname(),
                user.getEmail());
    }

    private static IndividualTicket individual(String[] parts, Map<String, User> holders) {
        User user = holders.computeIfAbsent(parts[4],
                dni -> new User(parts[5], parts[6], dni, parts[7], "", "", ""));
        return new IndividualTicket(parts[1], LocalDate.parse(parts[2]), new BigDecimal(parts[3]), user);
    }

    private static GroupTicket groupTicket(String[] parts, List<IndividualTicket> constituents,
                                           Map<String, Group> groups) {
        Group group = groups.computeIfAbsent(parts[4], id -> new Group(id, parts[5]));
        return new GroupTicket(parts[1], group, LocalDate.parse(parts[2]), new BigDecimal(parts[3]), constituents);
    }
}
