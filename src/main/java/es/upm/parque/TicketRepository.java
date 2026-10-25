package es.upm.parque;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in two files: individual tickets as {@code id;date;price;userDni;groupTicketId} (empty last
 * field for tickets bought individually) and group tickets as {@code id;date;groupId;total}.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";

    /**
     * Tickets restored from storage.
     */
    public record Loaded(List<IndividualTicket> individualTickets, List<GroupTicket> groupTickets) {
    }

    private final TextFileStore ticketStore;
    private final TextFileStore groupTicketStore;

    public TicketRepository(Path ticketsFile, Path groupTicketsFile) {
        this.ticketStore = new TextFileStore(ticketsFile);
        this.groupTicketStore = new TextFileStore(groupTicketsFile);
    }

    public Loaded load(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException {
        Map<String, String[]> groupTicketFields = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> internalTickets = new LinkedHashMap<>();
        for (String line : groupTicketStore.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 4) {
                groupTicketStore.warnSkipped(line, "expected 4 fields");
                continue;
            }
            groupTicketFields.put(f[0], f);
            internalTickets.put(f[0], new ArrayList<>());
        }

        List<IndividualTicket> individualTickets = new ArrayList<>();
        for (String line : ticketStore.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 5) {
                ticketStore.warnSkipped(line, "expected 5 fields");
                continue;
            }
            User user = userByDni.apply(f[3]);
            if (user == null) {
                ticketStore.warnSkipped(line, "unknown user " + f[3]);
                continue;
            }
            IndividualTicket ticket;
            try {
                ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), user, new BigDecimal(f[2]));
            } catch (DateTimeParseException | NumberFormatException e) {
                ticketStore.warnSkipped(line, "invalid date or price");
                continue;
            }
            if (f[4].isEmpty()) {
                individualTickets.add(ticket);
            } else if (internalTickets.containsKey(f[4])) {
                internalTickets.get(f[4]).add(ticket);
            } else {
                ticketStore.warnSkipped(line, "unknown group ticket " + f[4]);
            }
        }

        List<GroupTicket> groupTickets = new ArrayList<>();
        for (String[] f : groupTicketFields.values()) {
            String line = String.join(SEPARATOR, f);
            Group group = groupById.apply(f[2]);
            if (group == null) {
                groupTicketStore.warnSkipped(line, "unknown group " + f[2]);
                continue;
            }
            try {
                groupTickets.add(new GroupTicket(f[0], LocalDate.parse(f[1]), group, new BigDecimal(f[3]),
                        internalTickets.get(f[0])));
            } catch (DateTimeParseException | NumberFormatException e) {
                groupTicketStore.warnSkipped(line, "invalid date or price");
            }
        }
        return new Loaded(individualTickets, groupTickets);
    }

    public void saveAll(List<IndividualTicket> individualTickets, List<GroupTicket> groupTickets)
            throws IOException {
        List<String> ticketLines = new ArrayList<>();
        for (IndividualTicket ticket : individualTickets) {
            ticketLines.add(toLine(ticket, ""));
        }
        List<String> groupLines = new ArrayList<>();
        for (GroupTicket groupTicket : groupTickets) {
            groupLines.add(String.join(SEPARATOR, groupTicket.getId(), groupTicket.getDate().toString(),
                    groupTicket.getGroup().getId(), groupTicket.getPrice().toPlainString()));
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                ticketLines.add(toLine(ticket, groupTicket.getId()));
            }
        }
        groupTicketStore.writeLines(groupLines);
        ticketStore.writeLines(ticketLines);
    }

    private static String toLine(IndividualTicket ticket, String groupTicketId) {
        return String.join(SEPARATOR, ticket.getId(), ticket.getDate().toString(),
                ticket.getPrice().toPlainString(), ticket.getUser().getDni(), groupTicketId);
    }
}
