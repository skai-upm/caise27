package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Text-file ticket storage.
 *
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;dni;firstName;surnames;email
 * GROUP;id;date;groupId            (groupId empty once the group is deleted)
 * MEMBER;groupTicketId;id;basePrice;discount;dni;firstName;surnames;email
 * </pre>
 *
 * The user data stored with each ticket lets historical tickets of deleted users be rebuilt.
 */
public class TicketFileRepository {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";
    private static final int INDIVIDUAL_FIELDS = 9;
    private static final int GROUP_FIELDS = 4;
    private static final int MEMBER_FIELDS = 9;
    /** Index of the base price; price and user fields follow at the same positions in both line types. */
    private static final int PRICE = 3;

    private final Path file;
    private final PrintStream warnings;

    public TicketFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException {
        List<String[]> lines = new ArrayList<>();
        Map<String, List<String[]>> members = new LinkedHashMap<>();
        for (String line : TextFiles.readLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            if (isWellFormed(f)) {
                if (MEMBER.equals(f[0])) {
                    members.computeIfAbsent(f[1], k -> new ArrayList<>()).add(f);
                } else {
                    lines.add(f);
                }
            } else {
                warnings.println("WARNING: ignoring malformed ticket line in " + file);
            }
        }
        UserResolver users = new UserResolver(usersByDni);
        List<Ticket> tickets = new ArrayList<>();
        for (String[] f : lines) {
            LocalDate date = LocalDate.parse(f[2]);
            if (INDIVIDUAL.equals(f[0])) {
                tickets.add(individual(f[1], date, f, users));
            } else {
                List<IndividualTicket> composition = new ArrayList<>();
                for (String[] m : members.getOrDefault(f[1], List.of())) {
                    composition.add(individual(m[2], date, m, users));
                }
                Group group = f[3].isEmpty() ? null : groupsById.get(f[3]);
                tickets.add(new GroupTicket(f[1], date, group, composition));
            }
        }
        return tickets;
    }

    public void save(List<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket group) {
                lines.add(String.join(TextFiles.SEPARATOR, GROUP, group.getId(), group.getDate().toString(),
                        group.getGroup() == null ? "" : group.getGroup().getId()));
                for (IndividualTicket member : group.getIndividualTickets()) {
                    lines.add(String.join(TextFiles.SEPARATOR, MEMBER, group.getId(), member.getId(),
                            priceAndUser(member)));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(String.join(TextFiles.SEPARATOR, INDIVIDUAL, individual.getId(),
                        individual.getDate().toString(), priceAndUser(individual)));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static boolean isWellFormed(String[] f) {
        return switch (f[0]) {
            case INDIVIDUAL -> f.length == INDIVIDUAL_FIELDS && isDate(f[2]);
            case GROUP -> f.length == GROUP_FIELDS && isDate(f[2]);
            case MEMBER -> f.length == MEMBER_FIELDS;
            default -> false;
        };
    }

    private static boolean isDate(String text) {
        try {
            LocalDate.parse(text);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    private static String priceAndUser(IndividualTicket t) {
        User u = t.getUser();
        return String.join(TextFiles.SEPARATOR, t.getBasePrice().toPlainString(),
                Integer.toString(t.getDiscountPercent()), u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail());
    }

    /** Builds an individual ticket from fields: basePrice, discount, dni, firstName, surnames, email. */
    private static IndividualTicket individual(String id, LocalDate date, String[] f, UserResolver users) {
        BigDecimal basePrice = new BigDecimal(f[PRICE]);
        int discount = Integer.parseInt(f[PRICE + 1]);
        User user = users.resolve(f[PRICE + 2], f[PRICE + 3], f[PRICE + 4], f[PRICE + 5]);
        return new IndividualTicket(id, date, user, basePrice, discount);
    }

    /** Links tickets to active users by DNI; deleted users get one shared detached record per DNI. */
    private static final class UserResolver {
        private final Map<String, User> active;
        private final Map<String, User> detached = new HashMap<>();

        UserResolver(Map<String, User> active) {
            this.active = active;
        }

        User resolve(String dni, String firstName, String surnames, String email) {
            User user = active.get(dni);
            if (user != null) {
                return user;
            }
            return detached.computeIfAbsent(dni, d -> new User(firstName, surnames, d, email, "", "", ""));
        }
    }
}
