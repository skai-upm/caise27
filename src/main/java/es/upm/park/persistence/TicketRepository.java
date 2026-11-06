package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores top-level tickets. Individual: {@code I;id;date;price;email}. Group:
 * {@code G;id;date;groupId-or-empty;ticketId;email;price;...} with one triple per
 * composed ticket. Prices and emails are kept so historical tickets never depend
 * on their user or group still existing.
 */
public class TicketRepository {

    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";

    private final Path file;
    private final PrintStream warnings;

    public TicketRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /** Loads tickets, resolving group references against the given groups by id. */
    public List<Ticket> loadAll(Map<String, Group> groupsById) {
        return TextFile.parseAll(file, fields -> parse(fields, groupsById), warnings);
    }

    public void saveAll(List<Ticket> tickets) {
        TextFile.writeLines(file, tickets.stream().map(TicketRepository::format).toList());
    }

    private static Ticket parse(String[] fields, Map<String, Group> groupsById) {
        TextFile.requireFields(fields, 5);
        String id = fields[1];
        LocalDate date = LocalDate.parse(fields[2]);
        if (INDIVIDUAL.equals(fields[0])) {
            return new IndividualTicket(id, fields[4], date, new BigDecimal(fields[3]));
        }
        if (!GROUP.equals(fields[0]) || (fields.length - 4) % 3 != 0 || fields.length == 4) {
            throw new IllegalArgumentException("Unknown ticket line");
        }
        Group group = fields[3].isEmpty() ? null : groupsById.get(fields[3]);
        List<GroupTicket.Composed> composed = new ArrayList<>();
        for (int i = 4; i < fields.length; i += 3) {
            composed.add(new GroupTicket.Composed(fields[i], fields[i + 1], new BigDecimal(fields[i + 2])));
        }
        return GroupTicket.restore(id, group, date, composed);
    }

    private static String format(Ticket ticket) {
        String common = ticket.getId() + TextFile.SEPARATOR + ticket.getDate();
        if (ticket instanceof GroupTicket groupTicket) {
            StringBuilder line = new StringBuilder(GROUP).append(TextFile.SEPARATOR).append(common)
                    .append(TextFile.SEPARATOR)
                    .append(groupTicket.getGroup() == null ? "" : groupTicket.getGroup().getId());
            for (IndividualTicket composed : groupTicket.getComposition()) {
                line.append(TextFile.SEPARATOR).append(composed.getId())
                        .append(TextFile.SEPARATOR).append(composed.getOwnerEmail())
                        .append(TextFile.SEPARATOR).append(composed.getPrice());
            }
            return line.toString();
        }
        IndividualTicket individual = (IndividualTicket) ticket;
        return INDIVIDUAL + TextFile.SEPARATOR + common + TextFile.SEPARATOR + individual.getPrice()
                + TextFile.SEPARATOR + individual.getOwnerEmail();
    }
}
