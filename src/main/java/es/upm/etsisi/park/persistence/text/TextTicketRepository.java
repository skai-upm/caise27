package es.upm.etsisi.park.persistence.text;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Ticket;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.persistence.TicketRepository;

/**
 * Stores tickets, one record per line:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;state;dni;email;firstName;surnames
 * GROUP;id;date;groupId (empty once the group is deleted)
 * MEMBER;groupTicketId;id;date;basePrice;discount;state;dni;email;firstName;surnames
 * </pre>
 * {@code state} is {@code A} when the user was active at save time and
 * {@code F} for a former (deleted) user, whose snapshot is used to rebuild
 * historical tickets.
 */
public class TextTicketRepository implements TicketRepository {

    public static final String FILE_NAME = "tickets.txt";

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";
    private static final String ACTIVE = "A";
    private static final String FORMER = "F";

    private static final int INDIVIDUAL_FIELDS = 10;
    private static final int GROUP_FIELDS = 4;
    private static final int MEMBER_FIELDS = 11;

    private final Path file;
    private final PrintStream warnings;

    public TextTicketRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /** Loads the top-level tickets (individual and group tickets with their members). */
    @Override
    public List<Ticket> load(Map<String, User> activeUsersByDni, Map<String, Group> groupsById) throws IOException {
        Map<String, User> formerUsers = new HashMap<>();
        Map<String, Ticket> topLevel = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> groupMembers = new HashMap<>();
        Map<String, Group> groupOfTicket = new HashMap<>();
        Map<String, LocalDate> groupDates = new HashMap<>();

        List<String> lines = TextFiles.readLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                switch (f[0]) {
                    case INDIVIDUAL -> {
                        expect(f, INDIVIDUAL_FIELDS);
                        User user = resolveUser(f, 5, activeUsersByDni, formerUsers);
                        topLevel.put(f[1], new IndividualTicket(f[1], LocalDate.parse(f[2]), user,
                                new BigDecimal(f[3]), Integer.parseInt(f[4])));
                    }
                    case GROUP -> {
                        expect(f, GROUP_FIELDS);
                        topLevel.put(f[1], null);
                        groupDates.put(f[1], LocalDate.parse(f[2]));
                        groupMembers.put(f[1], new ArrayList<>());
                        if (!f[3].isEmpty()) {
                            groupOfTicket.put(f[1], groupsById.get(f[3]));
                        }
                    }
                    case MEMBER -> {
                        expect(f, MEMBER_FIELDS);
                        List<IndividualTicket> members = groupMembers.get(f[1]);
                        if (members == null) {
                            throw new IllegalArgumentException("member of unknown group ticket " + f[1]);
                        }
                        User user = resolveUser(f, 6, activeUsersByDni, formerUsers);
                        members.add(new IndividualTicket(f[2], LocalDate.parse(f[3]), user,
                                new BigDecimal(f[4]), Integer.parseInt(f[5])));
                    }
                    default -> throw new IllegalArgumentException("unknown record type");
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file);
            }
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Map.Entry<String, Ticket> entry : topLevel.entrySet()) {
            String id = entry.getKey();
            if (entry.getValue() != null) {
                tickets.add(entry.getValue());
            } else if (groupMembers.get(id).isEmpty()) {
                warnings.println("WARNING: group ticket " + id + " has no members and was skipped");
            } else {
                tickets.add(new GroupTicket(id, groupDates.get(id), groupOfTicket.get(id), groupMembers.get(id)));
            }
        }
        return tickets;
    }

    /** Replaces the file contents; users not in {@code activeUsers} are stored as former users. */
    @Override
    public void save(List<Ticket> tickets, Collection<User> activeUsers) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(String.join(TextFiles.SEPARATOR, GROUP, groupTicket.getId(),
                        groupTicket.getDate().toString(), group == null ? "" : group.getId()));
                for (IndividualTicket member : groupTicket.getMembers()) {
                    lines.add(MEMBER + TextFiles.SEPARATOR + groupTicket.getId() + TextFiles.SEPARATOR
                            + individualFields(member, activeUsers));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(INDIVIDUAL + TextFiles.SEPARATOR + individualFields(individual, activeUsers));
            }
        }
        TextFiles.write(file, lines);
    }

    private static String individualFields(IndividualTicket ticket, Collection<User> activeUsers) {
        User user = ticket.getUser();
        return String.join(TextFiles.SEPARATOR, ticket.getId(), ticket.getDate().toString(),
                ticket.getBasePrice().toPlainString(), String.valueOf(ticket.getDiscountPercent()),
                activeUsers.contains(user) ? ACTIVE : FORMER,
                user.getDni(), user.getEmail(), user.getFirstName(), user.getSurnames());
    }

    /** Fields from {@code start}: state, dni, email, firstName, surnames. */
    private static User resolveUser(String[] f, int start, Map<String, User> activeUsersByDni,
                                    Map<String, User> formerUsers) {
        String state = f[start];
        String dni = f[start + 1];
        if (ACTIVE.equals(state) && activeUsersByDni.containsKey(dni)) {
            return activeUsersByDni.get(dni);
        }
        if (!ACTIVE.equals(state) && !FORMER.equals(state)) {
            throw new IllegalArgumentException("unknown user state " + state);
        }
        return formerUsers.computeIfAbsent(dni,
                key -> new User(f[start + 3], f[start + 4], key, f[start + 2], "", "", ""));
    }

    private static void expect(String[] fields, int count) {
        if (fields.length != count) {
            throw new IllegalArgumentException("expected " + count + " fields");
        }
    }
}
