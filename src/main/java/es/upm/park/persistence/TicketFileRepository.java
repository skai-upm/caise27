package es.upm.park.persistence;

import es.upm.park.model.Group;
import es.upm.park.model.GroupDiscount;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in one file with two line kinds:
 * <pre>
 * GROUP;id;date;groupId
 * INDIVIDUAL;id;date;basePrice;discountPercent;parentGroupTicketId;dni;email;firstName;surnames
 * </pre>
 * The user snapshot (DNI, email, names) lets historical tickets of deleted
 * users be rebuilt; an empty group id marks a group that no longer exists.
 */
public class TicketFileRepository {

    private static final String GROUP_KIND = "GROUP";
    private static final String INDIVIDUAL_KIND = "INDIVIDUAL";
    private static final int GROUP_FIELDS = 4;
    private static final int INDIVIDUAL_FIELDS = 10;

    private final Path file;

    public TicketFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads top-level tickets (standalone individual tickets and group
     * tickets with their composition).
     *
     * @param userByDni  resolves active users; unknown DNIs become historical users
     * @param groupById  resolves existing groups; unknown ids leave the reference empty
     */
    public List<Ticket> loadAll(Function<String, User> userByDni, Function<String, Group> groupById) {
        List<String> lines = TextFiles.readLines(file);
        Map<String, GroupTicketData> groupTickets = new LinkedHashMap<>();
        List<Ticket> result = new ArrayList<>();
        Map<String, Integer> order = new LinkedHashMap<>();

        for (String line : lines) {
            String[] f = TextFiles.split(line);
            if (f[0].equals(GROUP_KIND) && f.length == GROUP_FIELDS && !f[1].isEmpty()) {
                try {
                    Group group = f[3].isEmpty() ? null : groupById.apply(f[3]);
                    groupTickets.put(f[1], new GroupTicketData(f[1], LocalDate.parse(f[2]), group));
                    order.put(f[1], order.size());
                } catch (RuntimeException e) {
                    TextFiles.warnMalformed(file, line);
                }
            } else if (!f[0].equals(INDIVIDUAL_KIND)) {
                TextFiles.warnMalformed(file, line);
            }
        }

        Map<String, Ticket> standalone = new LinkedHashMap<>();
        for (String line : lines) {
            String[] f = TextFiles.split(line);
            if (!f[0].equals(INDIVIDUAL_KIND)) {
                continue;
            }
            IndividualTicket ticket = parseIndividual(f, userByDni);
            if (ticket == null) {
                TextFiles.warnMalformed(file, line);
                continue;
            }
            String parentId = f[5];
            if (parentId.isEmpty()) {
                standalone.put(ticket.getId(), ticket);
                order.put(ticket.getId(), order.size());
            } else if (groupTickets.containsKey(parentId)) {
                groupTickets.get(parentId).composition.add(ticket);
            } else {
                TextFiles.warnMalformed(file, line);
            }
        }

        for (Map.Entry<String, Integer> entry : order.entrySet()) {
            String id = entry.getKey();
            if (standalone.containsKey(id)) {
                result.add(standalone.get(id));
            } else {
                GroupTicketData data = groupTickets.get(id);
                if (data.composition.isEmpty()) {
                    System.err.println("WARNING: group ticket " + id + " has no composing tickets; skipped");
                } else {
                    result.add(new GroupTicket(data.id, data.date, data.group, data.composition));
                }
            }
        }
        return result;
    }

    public void saveAll(Iterable<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(String.join(TextFiles.SEPARATOR, GROUP_KIND, groupTicket.getId(),
                        groupTicket.getDate().toString(), group == null ? "" : group.getId()));
                for (IndividualTicket component : groupTicket.getComposition()) {
                    lines.add(formatIndividual(component, groupTicket.getId()));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(formatIndividual(individual, ""));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static String formatIndividual(IndividualTicket ticket, String parentId) {
        User user = ticket.getUser();
        return String.join(TextFiles.SEPARATOR, INDIVIDUAL_KIND, ticket.getId(), ticket.getDate().toString(),
                ticket.getBasePrice().toPlainString(), Integer.toString(ticket.getDiscount().getPercent()),
                parentId, user.getDni(), user.getEmail(), user.getFirstName(), user.getSurnames());
    }

    private static IndividualTicket parseIndividual(String[] f, Function<String, User> userByDni) {
        if (f.length != INDIVIDUAL_FIELDS || f[1].isEmpty() || f[6].isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(f[2]);
            BigDecimal basePrice = new BigDecimal(f[3]);
            GroupDiscount discount = GroupDiscount.fromPercent(Integer.parseInt(f[4]));
            User user = userByDni.apply(f[6]);
            if (user == null) {
                user = new User(f[8], f[9], f[6], f[7], "", "", "");
            }
            return new IndividualTicket(f[1], date, user, basePrice, discount);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final class GroupTicketData {
        private final String id;
        private final LocalDate date;
        private final Group group;
        private final List<IndividualTicket> composition = new ArrayList<>();

        private GroupTicketData(String id, LocalDate date, Group group) {
            this.id = id;
            this.date = date;
            this.group = group;
        }
    }
}
