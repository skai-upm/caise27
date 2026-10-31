package es.upm.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in two files:
 * <ul>
 *   <li>tickets: {@code id;date;price;ownerGroupTicketId;dni;email;name;surname}
 *       (owner empty for stand-alone tickets);</li>
 *   <li>group tickets: {@code id;date;price;groupId;groupName}.</li>
 * </ul>
 * The holder's email and name are kept so that historical tickets survive the
 * deletion of their user; a deleted group is likewise rebuilt from its id and name.
 */
public class TicketRepository {

    private static final int TICKET_FIELDS = 8;
    private static final int GROUP_TICKET_FIELDS = 5;

    private final StoragePaths paths;

    public TicketRepository(StoragePaths paths) {
        this.paths = paths;
    }

    /** Tickets loaded from storage. */
    public record Tickets(List<IndividualTicket> individual, List<GroupTicket> group) {
    }

    public Tickets loadAll(Function<String, User> userByDni, Function<String, Group> groupById)
            throws IOException {
        List<IndividualTicket> individual = new ArrayList<>();
        Map<String, List<IndividualTicket>> constituents = new LinkedHashMap<>();
        for (String[] f : TextFileStore.readRecords(paths.tickets(), TICKET_FIELDS)) {
            User user = userByDni.apply(f[4]);
            if (user == null) {
                user = new User(f[6], f[7], f[4], f[5], "", "", "");
            }
            IndividualTicket ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), user);
            if (f[3].isEmpty()) {
                individual.add(ticket);
            } else {
                constituents.computeIfAbsent(f[3], k -> new ArrayList<>()).add(ticket);
            }
        }

        List<GroupTicket> group = new ArrayList<>();
        for (String[] f : TextFileStore.readRecords(paths.groupTickets(), GROUP_TICKET_FIELDS)) {
            Group owner = groupById.apply(f[3]);
            if (owner == null) {
                owner = new Group(f[3], f[4]);
            }
            group.add(new GroupTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), owner,
                    constituents.getOrDefault(f[0], List.of())));
        }
        return new Tickets(individual, group);
    }

    public void saveAll(List<IndividualTicket> individual, List<GroupTicket> group) throws IOException {
        List<String[]> ticketRecords = new ArrayList<>();
        List<String[]> groupRecords = new ArrayList<>();
        for (IndividualTicket ticket : individual) {
            ticketRecords.add(toRecord(ticket, ""));
        }
        for (GroupTicket groupTicket : group) {
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                ticketRecords.add(toRecord(ticket, groupTicket.getId()));
            }
            groupRecords.add(new String[] {groupTicket.getId(), groupTicket.getDate().toString(),
                    groupTicket.getPrice().toPlainString(), groupTicket.getGroup().getId(),
                    groupTicket.getGroup().getName()});
        }
        TextFileStore.writeRecords(paths.tickets(), ticketRecords);
        TextFileStore.writeRecords(paths.groupTickets(), groupRecords);
    }

    private static String[] toRecord(IndividualTicket ticket, String owner) {
        User user = ticket.getUser();
        return new String[] {ticket.getId(), ticket.getDate().toString(), ticket.getPrice().toPlainString(),
                owner, user.getDni(), user.getEmail(), user.getName(), user.getSurname()};
    }
}
