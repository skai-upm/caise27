package es.upm.amusementpark.persistence.text;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.TicketRepository;

/**
 * Stores tickets. Each record keeps a snapshot of the user (DNI, names, email) so that historical
 * tickets survive the deletion of their user:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;dni;firstName;surnames;email
 * GROUP;id;date;groupId;basePrice;discountPercent
 * MEMBER;groupTicketId;dni;firstName;surnames;email
 * </pre>
 */
public class TextTicketRepository implements TicketRepository {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";

    private final Path file;

    public TextTicketRepository(Path file) {
        this.file = file;
    }

    @Override
    public Map<String, Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) {
        Map<String, Ticket> tickets = new LinkedHashMap<>();
        Map<String, String[]> groupHeaders = new LinkedHashMap<>();
        Map<String, List<User>> groupMembers = new LinkedHashMap<>();
        for (String[] f : TextFiles.readRecords(file)) {
            try {
                switch (f[0]) {
                    case INDIVIDUAL -> {
                        User user = resolveUser(usersByDni, f[4], f[5], f[6], f[7]);
                        tickets.put(f[1], new IndividualTicket(f[1], LocalDate.parse(f[2]), user, new BigDecimal(f[3]), 0));
                    }
                    case GROUP -> {
                        groupHeaders.put(f[1], f);
                        groupMembers.put(f[1], new ArrayList<>());
                        tickets.put(f[1], null);
                    }
                    case MEMBER -> groupMembers.get(f[1]).add(resolveUser(usersByDni, f[2], f[3], f[4], f[5]));
                    default -> TextFiles.warnMalformed(file);
                }
            } catch (RuntimeException e) {
                TextFiles.warnMalformed(file);
            }
        }
        for (String[] f : groupHeaders.values()) {
            try {
                if (groupMembers.get(f[1]).isEmpty()) {
                    throw new IllegalStateException("group ticket without members");
                }
                Group group = f[3].isEmpty() ? null : groupsById.get(f[3]);
                tickets.put(f[1], new GroupTicket(f[1], LocalDate.parse(f[2]), group, groupMembers.get(f[1]),
                        new BigDecimal(f[4]), Integer.parseInt(f[5])));
            } catch (RuntimeException e) {
                tickets.remove(f[1]);
                TextFiles.warnMalformed(file);
            }
        }
        return tickets;
    }

    @Override
    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                lines.add(join(INDIVIDUAL, it.getId(), it.getDate().toString(), it.getBasePrice().toPlainString(),
                        snapshot(it.getUser())));
            } else if (t instanceof GroupTicket gt) {
                IndividualTicket first = gt.getComposition().get(0);
                String groupId = gt.getGroup() == null ? "" : gt.getGroup().getId();
                lines.add(join(GROUP, gt.getId(), gt.getDate().toString(), groupId,
                        first.getBasePrice().toPlainString(), String.valueOf(first.getDiscountPercent())));
                for (IndividualTicket member : gt.getComposition()) {
                    lines.add(join(MEMBER, gt.getId(), snapshot(member.getUser())));
                }
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static User resolveUser(Map<String, User> usersByDni, String dni, String firstName,
                                    String surnames, String email) {
        User active = usersByDni.get(dni);
        if (active != null) {
            return active;
        }
        // The user was deleted: rebuild a detached record so the historical ticket survives.
        return new User(firstName, surnames, dni, email, "", "", "");
    }

    private static String snapshot(User u) {
        return join(u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail());
    }

    private static String join(String... parts) {
        return TextFiles.join(parts);
    }
}
