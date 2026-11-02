package es.upm.etsisi.park;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores tickets, one record per line:
 * <pre>
 * I;id;date;basePrice;discount;dni;email;firstName;surnames   standalone individual ticket
 * G;id;date;groupId                                           group ticket (groupId empty once deleted)
 * C;id;date;basePrice;discount;dni;email;firstName;surnames   constituent of the preceding G line
 * </pre>
 * Each individual record keeps a snapshot of its holder so historical tickets survive user deletion.
 */
public class TicketRepository {

    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";
    private static final String COMPONENT = "C";
    private static final int INDIVIDUAL_FIELDS = 9;
    private static final int GROUP_FIELDS = 4;

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) {
        List<Ticket> tickets = new ArrayList<>();
        PendingGroup pending = null;
        for (String line : TextFile.readLines(file)) {
            String[] f = TextFile.fields(line);
            String kind = f[0];
            if (COMPONENT.equals(kind) && pending != null && f.length == INDIVIDUAL_FIELDS) {
                pending.composition.add(individual(f, usersByDni));
                continue;
            }
            addPending(tickets, pending);
            pending = null;
            if (INDIVIDUAL.equals(kind) && f.length == INDIVIDUAL_FIELDS) {
                tickets.add(individual(f, usersByDni));
            } else if (GROUP.equals(kind) && f.length == GROUP_FIELDS) {
                pending = new PendingGroup(f[1], ValueParser.parseDate(f[2]), groupsById.get(f[3]));
            } else {
                TextFile.warnMalformed(file, line);
            }
        }
        addPending(tickets, pending);
        return tickets;
    }

    private void addPending(List<Ticket> tickets, PendingGroup pending) {
        if (pending == null) {
            return;
        }
        if (pending.composition.isEmpty()) {
            TextFile.warnMalformed(file, GROUP + TextFile.SEPARATOR + pending.id);
            return;
        }
        tickets.add(new GroupTicket(pending.id, pending.date, pending.group, pending.composition));
    }

    private static IndividualTicket individual(String[] f, Map<String, User> usersByDni) {
        User holder = usersByDni.get(f[5]);
        if (holder == null) {
            holder = new User(f[7], f[8], f[5], f[6], "", "", "");
        }
        return new IndividualTicket(f[1], ValueParser.parseDate(f[2]), holder, new BigDecimal(f[3]), Integer.parseInt(f[4]));
    }

    List<String> toLines(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                String groupId = g.getGroup() == null ? "" : g.getGroup().getId();
                lines.add(TextFile.join(GROUP, g.getId(), ValueParser.formatDate(g.getDate()), groupId));
                for (IndividualTicket c : g.getComposition()) {
                    lines.add(individualLine(COMPONENT, c));
                }
            } else if (t instanceof IndividualTicket i) {
                lines.add(individualLine(INDIVIDUAL, i));
            }
        }
        return lines;
    }

    private static String individualLine(String kind, IndividualTicket t) {
        User u = t.getUser();
        return TextFile.join(kind, t.getId(), ValueParser.formatDate(t.getDate()), t.getBasePrice().toPlainString(),
                Integer.toString(t.getDiscountPercent()), u.getDni(), u.getEmail(), u.getFirstName(), u.getSurnames());
    }

    private static final class PendingGroup {
        private final String id;
        private final LocalDate date;
        private final Group group;
        private final List<IndividualTicket> composition = new ArrayList<>();

        PendingGroup(String id, LocalDate date, Group group) {
            this.id = id;
            this.date = date;
            this.group = group;
        }
    }
}
