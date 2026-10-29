package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores tickets. Each line starts with its kind:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames
 * GROUP;id;date;basePrice;discount;groupIdOrEmpty
 * MEMBER;groupTicketId;dni;email;firstName;surnames
 * </pre>
 * User data is stored with each ticket so historical tickets survive the deletion of their user.
 */
public class TicketRepository {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads tickets, resolving users among active users (or rebuilding deleted users from the stored
     * data) and groups among the existing groups.
     */
    public List<Ticket> load(UserService users, List<Group> groups) {
        Map<String, User> detached = new HashMap<>();
        Map<String, String[]> groupHeaders = new LinkedHashMap<>();
        Map<String, List<User>> groupMembers = new HashMap<>();
        List<Object> order = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            try {
                switch (f[0]) {
                    case INDIVIDUAL -> {
                        check(f.length == 9);
                        order.add(new IndividualTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]),
                                Integer.parseInt(f[4]), resolve(users, detached, f[5], f[6], f[7], f[8])));
                    }
                    case GROUP -> {
                        check(f.length == 6 && !groupHeaders.containsKey(f[1]));
                        groupHeaders.put(f[1], f);
                        groupMembers.put(f[1], new ArrayList<>());
                        order.add(f[1]);
                    }
                    case MEMBER -> {
                        check(f.length == 6 && groupMembers.containsKey(f[1]));
                        groupMembers.get(f[1]).add(resolve(users, detached, f[2], f[3], f[4], f[5]));
                    }
                    default -> throw new IllegalArgumentException();
                }
            } catch (RuntimeException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        List<Ticket> tickets = new ArrayList<>();
        for (Object entry : order) {
            if (entry instanceof Ticket ticket) {
                tickets.add(ticket);
                continue;
            }
            String[] f = groupHeaders.get((String) entry);
            tickets.add(new GroupTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]), Integer.parseInt(f[4]),
                    findGroup(groups, f[5]), groupMembers.get(f[1])));
        }
        return tickets;
    }

    public void save(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            String date = t.getDate().toString();
            String base = t.getBasePrice().toPlainString();
            String discount = String.valueOf(t.getDiscountPercent());
            if (t instanceof GroupTicket gt) {
                String groupId = gt.getGroup() == null ? "" : gt.getGroup().getId();
                lines.add(String.join(TextFiles.SEPARATOR, GROUP, t.getId(), date, base, discount, groupId));
                for (User u : gt.getUsers()) {
                    lines.add(String.join(TextFiles.SEPARATOR, MEMBER, t.getId(), u.getDni(), u.getEmail(),
                            u.getFirstName(), u.getSurnames()));
                }
            } else if (t instanceof IndividualTicket it) {
                User u = it.getUser();
                lines.add(String.join(TextFiles.SEPARATOR, INDIVIDUAL, t.getId(), date, base, discount,
                        u.getDni(), u.getEmail(), u.getFirstName(), u.getSurnames()));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    private static User resolve(UserService users, Map<String, User> detached, String dni, String email,
                                String firstName, String surnames) {
        User active = users.findByDni(dni);
        if (active != null) {
            return active;
        }
        return detached.computeIfAbsent(dni, d -> new User(firstName, surnames, d, email, "", "", ""));
    }

    private static Group findGroup(List<Group> groups, String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }
}
