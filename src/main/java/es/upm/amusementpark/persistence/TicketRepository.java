package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.ticket.GroupTicket;
import es.upm.amusementpark.model.ticket.IndividualTicket;
import es.upm.amusementpark.model.ticket.Ticket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Stores all tickets in one file. Line formats ({@code ;}-separated):
 * <ul>
 *   <li>{@code G;id;date;groupId} — group ticket ({@code groupId} empty once the group is deleted)</li>
 *   <li>{@code I;id;date;basePrice;discountPercent;parentId;state;dni;email;firstName;surnames} —
 *       individual ticket; {@code parentId} is the containing group ticket or empty; {@code state} is
 *       {@code A} if the user was active when saved, {@code H} if the user had been deleted.</li>
 * </ul>
 * Users are stored by identity so historical tickets of deleted users can be rebuilt.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";
    private static final String GROUP_KIND = "G";
    private static final String INDIVIDUAL_KIND = "I";
    private static final String ACTIVE_USER = "A";
    private static final String HISTORICAL_USER = "H";
    private static final int GROUP_FIELDS = 4;
    private static final int INDIVIDUAL_FIELDS = 11;

    private final Path path;
    private final PrintStream warnings;

    public TicketRepository(Path path, PrintStream warnings) {
        this.path = path;
        this.warnings = warnings;
    }

    /**
     * Loads all tickets: each group ticket is followed by the individual tickets of its composition.
     */
    public List<Ticket> load(Map<String, User> activeUsersByDni, Map<String, Group> groupsById) throws IOException {
        List<String[]> groupLines = new ArrayList<>();
        List<String[]> individualLines = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(path)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f[0].equals(GROUP_KIND) && f.length == GROUP_FIELDS) {
                groupLines.add(f);
            } else if (f[0].equals(INDIVIDUAL_KIND) && f.length == INDIVIDUAL_FIELDS) {
                individualLines.add(f);
            } else {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + path);
            }
        }

        Map<String, User> detachedUsersByDni = new HashMap<>();
        Map<String, List<IndividualTicket>> compositions = new LinkedHashMap<>();
        List<Ticket> standalone = new ArrayList<>();
        for (String[] f : individualLines) {
            try {
                User user = resolveUser(f, activeUsersByDni, detachedUsersByDni);
                IndividualTicket ticket = new IndividualTicket(f[1], LocalDate.parse(f[2]), user,
                        new BigDecimal(f[3]), Integer.parseInt(f[4]));
                if (f[5].isEmpty()) {
                    standalone.add(ticket);
                } else {
                    compositions.computeIfAbsent(f[5], k -> new ArrayList<>()).add(ticket);
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                warnings.println("WARNING: skipping malformed ticket " + f[1] + " in " + path);
            }
        }

        List<Ticket> tickets = new ArrayList<>();
        for (String[] f : groupLines) {
            List<IndividualTicket> composition = compositions.remove(f[1]);
            if (composition == null) {
                warnings.println("WARNING: skipping group ticket " + f[1] + " without composition in " + path);
                continue;
            }
            try {
                Group group = f[3].isEmpty() ? null : groupsById.get(f[3]);
                tickets.add(new GroupTicket(f[1], LocalDate.parse(f[2]), group, composition));
                tickets.addAll(composition);
            } catch (DateTimeParseException e) {
                warnings.println("WARNING: skipping malformed ticket " + f[1] + " in " + path);
            }
        }
        for (String orphanParent : compositions.keySet()) {
            warnings.println("WARNING: skipping tickets of unknown group ticket " + orphanParent + " in " + path);
        }
        tickets.addAll(standalone);
        return tickets;
    }

    /**
     * Writes all tickets. {@code isActiveUser} tells whether a ticket's user is still registered.
     */
    public void saveAll(Collection<Ticket> tickets, Predicate<User> isActiveUser) throws IOException {
        Map<IndividualTicket, String> parentOf = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                for (IndividualTicket member : groupTicket.getComposition()) {
                    parentOf.put(member, groupTicket.getId());
                }
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Ticket ticket : tickets) {
                if (ticket instanceof GroupTicket groupTicket) {
                    Group group = groupTicket.getGroup();
                    writer.write(String.join(SEPARATOR, GROUP_KIND, ticket.getId(), ticket.getDate().toString(),
                            group == null ? "" : group.getId()));
                } else if (ticket instanceof IndividualTicket individual) {
                    User user = individual.getUser();
                    writer.write(String.join(SEPARATOR, INDIVIDUAL_KIND, ticket.getId(),
                            ticket.getDate().toString(), individual.getBasePrice().toPlainString(),
                            String.valueOf(individual.getDiscountPercent()),
                            parentOf.getOrDefault(individual, ""),
                            isActiveUser.test(user) ? ACTIVE_USER : HISTORICAL_USER,
                            user.getDni(), user.getEmail(), user.getFirstName(), user.getSurnames()));
                }
                writer.newLine();
            }
        }
    }

    private static User resolveUser(String[] f, Map<String, User> activeUsersByDni,
                                    Map<String, User> detachedUsersByDni) {
        String dni = f[7];
        if (f[6].equals(ACTIVE_USER)) {
            User active = activeUsersByDni.get(dni);
            if (active != null) {
                return active;
            }
        }
        return detachedUsersByDni.computeIfAbsent(dni, d -> new User(f[9], f[10], d, f[8], "", "", ""));
    }
}
