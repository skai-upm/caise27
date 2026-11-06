package es.upm.park.persistence;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.park.model.Group;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;

/**
 * Stores tickets in text lines:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;dni;firstName;surnames;email
 * GROUP;id;date;basePrice;discount;attachedGroupId;groupId
 * MEMBER;dni;firstName;surnames;email     (one per constituent, after GROUP)
 * </pre>
 * The user snapshot lets historical tickets of deleted users be rebuilt.
 */
public class TicketFileStore {

    static final String INDIVIDUAL = "INDIVIDUAL";
    static final String GROUP = "GROUP";
    static final String MEMBER = "MEMBER";

    private final Path file;

    public TicketFileStore(Path file) {
        this.file = file;
    }

    /**
     * Rebuilds the tickets, linking live users and groups when they exist.
     */
    public List<Ticket> load(Function<String, Optional<User>> userByDni,
            Function<String, Optional<Group>> groupById) {
        List<Ticket> tickets = new ArrayList<>();
        List<String[]> pendingGroup = null;
        List<String[]> pendingMembers = new ArrayList<>();
        for (String line : TextFile.read(file)) {
            String[] f = TextFile.fields(line);
            if (MEMBER.equals(f[0]) && f.length == 5 && pendingGroup != null) {
                pendingMembers.add(f);
                continue;
            }
            addGroup(tickets, pendingGroup, pendingMembers, userByDni, groupById);
            pendingGroup = null;
            pendingMembers = new ArrayList<>();
            if (INDIVIDUAL.equals(f[0]) && f.length == 9) {
                parseIndividual(f, userByDni).ifPresent(tickets::add);
            } else if (GROUP.equals(f[0]) && f.length == 7) {
                pendingGroup = new ArrayList<>();
                pendingGroup.add(f);
            }
        }
        addGroup(tickets, pendingGroup, pendingMembers, userByDni, groupById);
        return tickets;
    }

    public void save(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                lines.add(String.join(TextFile.SEPARATOR, GROUP, g.getId(), g.getDate().toString(),
                        g.getBasePrice().toPlainString(), g.getDiscount().toPlainString(),
                        g.getGroup() == null ? "" : g.getGroup().getId(), g.getGroupId()));
                for (IndividualTicket c : g.getComposition()) {
                    lines.add(MEMBER + TextFile.SEPARATOR + snapshot(c.getUser()));
                }
            } else if (t instanceof IndividualTicket i) {
                lines.add(String.join(TextFile.SEPARATOR, INDIVIDUAL, i.getId(), i.getDate().toString(),
                        i.getBasePrice().toPlainString(), i.getDiscount().toPlainString(), snapshot(i.getUser())));
            }
        }
        TextFile.write(file, lines);
    }

    private static String snapshot(User u) {
        return String.join(TextFile.SEPARATOR, u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail());
    }

    private static User user(String[] f, int from, Function<String, Optional<User>> userByDni) {
        return userByDni.apply(f[from]).orElseGet(
                () -> new User(f[from + 1], f[from + 2], f[from], f[from + 3], "", "", ""));
    }

    private static Optional<Ticket> parseIndividual(String[] f, Function<String, Optional<User>> userByDni) {
        try {
            return Optional.of(new IndividualTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]),
                    new BigDecimal(f[4]), user(f, 5, userByDni)));
        } catch (DateTimeParseException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static void addGroup(List<Ticket> tickets, List<String[]> pendingGroup, List<String[]> members,
            Function<String, Optional<User>> userByDni, Function<String, Optional<Group>> groupById) {
        if (pendingGroup == null || members.isEmpty()) {
            return;
        }
        String[] f = pendingGroup.get(0);
        try {
            List<User> users = new ArrayList<>();
            members.forEach(m -> users.add(user(m, 1, userByDni)));
            Group group = f[5].isEmpty() ? null : groupById.apply(f[5]).orElse(null);
            tickets.add(new GroupTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]), new BigDecimal(f[4]),
                    group, f[6], users));
        } catch (DateTimeParseException | NumberFormatException e) {
            // malformed record: skipped like any other unreadable line
        }
    }
}
