package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.Ticket;
import es.upm.etsisi.park.model.User;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores tickets as typed lines:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;dni;email;firstName;surnames
 * GROUP;id;date;basePrice;groupId (empty once the group is deleted)
 * MEMBER;groupTicketId;dni;email;firstName;surnames
 * </pre>
 * Holder identity is stored so historical tickets survive the deletion of their user.
 */
public class TicketFileRepository {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";

    private final Path file;
    private final PrintStream warnings;

    public TicketFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Ticket> loadAll(Function<String, Optional<User>> userByDni, Function<String, Optional<Group>> groupById) {
        HolderResolver holders = new HolderResolver(userByDni);
        Map<String, Ticket> tickets = new LinkedHashMap<>();
        Map<String, String[]> groupHeaders = new LinkedHashMap<>();
        Map<String, List<User>> groupMembers = new HashMap<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = TextFiles.fields(line);
            try {
                switch (parts[0]) {
                    case INDIVIDUAL -> {
                        requireLength(parts, 8);
                        User user = holders.resolve(parts[4], parts[5], parts[6], parts[7]);
                        tickets.put(parts[1], new IndividualTicket(parts[1], LocalDate.parse(parts[2]),
                                new BigDecimal(parts[3]), user));
                    }
                    case GROUP -> {
                        requireLength(parts, 5);
                        groupHeaders.put(parts[1], parts);
                        groupMembers.put(parts[1], new ArrayList<>());
                        tickets.put(parts[1], null);
                    }
                    case MEMBER -> {
                        requireLength(parts, 6);
                        List<User> members = groupMembers.get(parts[1]);
                        if (members == null) {
                            throw new IllegalArgumentException("member of unknown group ticket " + parts[1]);
                        }
                        members.add(holders.resolve(parts[2], parts[3], parts[4], parts[5]));
                    }
                    default -> throw new IllegalArgumentException("unknown record type " + parts[0]);
                }
            } catch (RuntimeException e) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + file + ": "
                        + e.getMessage());
            }
        }
        for (Map.Entry<String, String[]> entry : groupHeaders.entrySet()) {
            String[] header = entry.getValue();
            Group group = header[4].isEmpty() ? null : groupById.apply(header[4]).orElse(null);
            tickets.put(entry.getKey(), new GroupTicket(header[1], LocalDate.parse(header[2]),
                    new BigDecimal(header[3]), group, groupMembers.get(entry.getKey())));
        }
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket != null) {
                result.add(ticket);
            }
        }
        return result;
    }

    public void saveAll(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(String.join(TextFiles.SEPARATOR, GROUP, ticket.getId(), ticket.getDate().toString(),
                        ticket.getBasePrice().toPlainString(), group == null ? "" : group.getId()));
                for (IndividualTicket component : groupTicket.getTickets()) {
                    lines.add(String.join(TextFiles.SEPARATOR, MEMBER, ticket.getId(), holder(component.getUser())));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(String.join(TextFiles.SEPARATOR, INDIVIDUAL, ticket.getId(), ticket.getDate().toString(),
                        ticket.getBasePrice().toPlainString(), holder(individual.getUser())));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static String holder(User user) {
        return String.join(TextFiles.SEPARATOR, user.getDni(), user.getEmail(), user.getFirstName(),
                user.getSurnames());
    }

    private static void requireLength(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("expected " + expected + " fields");
        }
    }

    /**
     * Resolves ticket holders to registered users, or to one shared detached user per DNI
     * when the user has been deleted.
     */
    private static final class HolderResolver {

        private final Function<String, Optional<User>> userByDni;
        private final Map<String, User> detached = new HashMap<>();

        HolderResolver(Function<String, Optional<User>> userByDni) {
            this.userByDni = userByDni;
        }

        User resolve(String dni, String email, String firstName, String surnames) {
            Optional<User> registered = userByDni.apply(dni);
            if (registered.isPresent()) {
                return registered.get();
            }
            return detached.computeIfAbsent(dni, d -> new User(firstName, surnames, d, email, "", "", ""));
        }
    }
}
