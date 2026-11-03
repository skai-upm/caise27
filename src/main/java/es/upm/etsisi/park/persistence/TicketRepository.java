package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.GroupTicket;
import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Ticket;
import es.upm.etsisi.park.business.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores tickets, one record per line, identified by the first field:
 * <ul>
 * <li>{@code I;id;date;basePrice;discount;dni;email;firstName;surnames} standalone individual ticket</li>
 * <li>{@code G;id;date;groupId} group ticket ({@code groupId} empty once the group is deleted)</li>
 * <li>{@code C;groupTicketId;id;date;basePrice;discount;dni;email;firstName;surnames} composed ticket</li>
 * </ul>
 * The holder snapshot lets historical tickets be rebuilt after their user is deleted.
 */
public class TicketRepository extends TextFileRepository {

    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";
    private static final String COMPOSED = "C";

    public TicketRepository(Path file) {
        super(file);
    }

    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        List<GroupRecord> groupRecords = new ArrayList<>();
        for (String line : readLines()) {
            try {
                parse(line, usersByDni, groupsById, tickets, groupRecords);
            } catch (DateTimeParseException | NumberFormatException e) {
                warnMalformed(line);
            }
        }
        for (GroupRecord r : groupRecords) {
            tickets.add(new GroupTicket(r.id, r.date, r.group, r.composition));
        }
        return tickets;
    }

    private void parse(String line, Map<String, User> usersByDni, Map<String, Group> groupsById,
                       List<Ticket> tickets, List<GroupRecord> groupRecords) {
        String type = line.split(SEPARATOR, 2)[0];
        if (INDIVIDUAL.equals(type)) {
            String[] f = fields(line, 9);
            if (f.length == 0) {
                warnMalformed(line);
                return;
            }
            tickets.add(individual(f, 1, usersByDni));
        } else if (GROUP.equals(type)) {
            String[] f = fields(line, 4);
            if (f.length == 0) {
                warnMalformed(line);
                return;
            }
            Group group = f[3].isEmpty() ? null : groupsById.get(f[3]);
            groupRecords.add(new GroupRecord(f[1], LocalDate.parse(f[2]), group, new ArrayList<>()));
        } else if (COMPOSED.equals(type)) {
            String[] f = fields(line, 10);
            GroupRecord owner = f.length == 0 ? null : find(groupRecords, f[1]);
            if (owner == null) {
                warnMalformed(line);
                return;
            }
            owner.composition.add(individual(f, 2, usersByDni));
        } else {
            warnMalformed(line);
        }
    }

    private static GroupRecord find(List<GroupRecord> records, String id) {
        for (GroupRecord r : records) {
            if (r.id.equals(id)) {
                return r;
            }
        }
        return null;
    }

    /** Builds an individual ticket from fields starting at {@code offset} (the ticket id). */
    private static IndividualTicket individual(String[] f, int offset, Map<String, User> usersByDni) {
        String id = f[offset];
        LocalDate date = LocalDate.parse(f[offset + 1]);
        BigDecimal basePrice = new BigDecimal(f[offset + 2]);
        int discount = Integer.parseInt(f[offset + 3]);
        String dni = f[offset + 4];
        User holder = usersByDni.get(dni);
        if (holder == null) {
            holder = new User(f[offset + 6], f[offset + 7], dni, f[offset + 5], "", "", "");
        }
        return new IndividualTicket(id, date, holder, basePrice, discount);
    }

    public void saveAll(Collection<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                String groupId = g.getGroup() == null ? "" : g.getGroup().getId();
                lines.add(String.join(SEPARATOR, GROUP, g.getId(), g.getDate().toString(), groupId));
                for (IndividualTicket c : g.getComposition()) {
                    lines.add(COMPOSED + SEPARATOR + g.getId() + SEPARATOR + individualFields(c));
                }
            } else if (t instanceof IndividualTicket i) {
                lines.add(INDIVIDUAL + SEPARATOR + individualFields(i));
            }
        }
        writeLines(lines);
    }

    private static String individualFields(IndividualTicket t) {
        User u = t.getUser();
        return String.join(SEPARATOR, t.getId(), t.getDate().toString(), t.getBasePrice().toPlainString(),
                String.valueOf(t.getDiscountPercent()), u.getDni(), u.getEmail(), u.getFirstName(), u.getSurnames());
    }

    private record GroupRecord(String id, LocalDate date, Group group, List<IndividualTicket> composition) {
    }
}
