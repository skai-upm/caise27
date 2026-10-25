package es.upm.parque;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Stores group tickets as {@code id;groupId;date;price} and individual tickets as
 * {@code id;date;dni;price;groupTicketId} (empty group ticket id for tickets bought individually).
 */
public class TicketRepository {

    private static final Logger LOGGER = Logger.getLogger(TicketRepository.class.getName());

    private static final int TICKET_FIELDS = 5;
    private static final int GROUP_TICKET_FIELDS = 4;

    private final Path ticketsFile;
    private final Path groupTicketsFile;

    public TicketRepository(Path ticketsFile, Path groupTicketsFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
    }

    public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) {
        Map<String, GroupTicket> groupTickets = new LinkedHashMap<>();
        for (String[] f : TextFile.readRecords(groupTicketsFile, GROUP_TICKET_FIELDS)) {
            groupTickets.put(f[0], new GroupTicket(f[0], LocalDate.parse(f[2]), new BigDecimal(f[3]),
                    groupById.apply(f[1]), f[1]));
        }
        List<Ticket> tickets = new ArrayList<>(groupTickets.values());
        for (String[] f : TextFile.readRecords(ticketsFile, TICKET_FIELDS)) {
            GroupTicket owner = f[4].isEmpty() ? null : groupTickets.get(f[4]);
            if (!f[4].isEmpty() && owner == null) {
                LOGGER.warning(() -> "ticket " + f[0] + " references unknown group ticket " + f[4]);
                continue;
            }
            IndividualTicket ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[3]),
                    userByDni.apply(f[2]), f[2], owner);
            if (owner != null) {
                owner.addTicket(ticket);
            }
            tickets.add(ticket);
        }
        return tickets;
    }

    public void save(Collection<Ticket> tickets) {
        List<String> ticketLines = new ArrayList<>();
        List<String> groupTicketLines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                groupTicketLines.add(TextFile.join(g.getId(), g.getGroupId(), g.getDate().toString(),
                        g.getPrice().toPlainString()));
            } else if (t instanceof IndividualTicket i) {
                String groupTicketId = i.isPartOfGroupTicket() ? i.getGroupTicket().getId() : "";
                ticketLines.add(TextFile.join(i.getId(), i.getDate().toString(), i.getOwnerDni(),
                        i.getPrice().toPlainString(), groupTicketId));
            }
        }
        TextFile.writeRecords(groupTicketsFile, groupTicketLines);
        TextFile.writeRecords(ticketsFile, ticketLines);
    }
}
