package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.TicketRepository;


import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores individual tickets ({@code id;dni;date;price}) and group tickets
 * ({@code id;groupId;date;price;ticketId1,ticketId2,...}) in two files.
 * References that no longer resolve are restored with detached placeholders so history is kept.
 */
public class TextTicketRepository implements TicketRepository {

    public static final String TICKETS_FILE_NAME = "tickets.txt";
    public static final String GROUP_TICKETS_FILE_NAME = "group_tickets.txt";

    private static final String LIST_SEPARATOR = ",";

    private final Path ticketsFile;
    private final Path groupTicketsFile;

    public TextTicketRepository(Path ticketsFile, Path groupTicketsFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
    }

    @Override
    public Loaded load(Function<String, User> usersByDni, Function<String, Group> groupsById) {
        Map<String, IndividualTicket> individual = new LinkedHashMap<>();
        for (String[] fields : TextStore.readRecords(ticketsFile)) {
            try {
                if (fields.length != 4) {
                    throw new IllegalArgumentException();
                }
                User user = usersByDni.apply(fields[1]);
                if (user == null) {
                    user = new User("", "", fields[1], "", "", "", "");
                }
                individual.put(fields[0], new IndividualTicket(fields[0], user,
                        LocalDate.parse(fields[2]), new BigDecimal(fields[3])));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                TextStore.warnMalformed(ticketsFile, fields);
            }
        }
        List<GroupTicket> groupTickets = new ArrayList<>();
        for (String[] fields : TextStore.readRecords(groupTicketsFile)) {
            try {
                if (fields.length != 5) {
                    throw new IllegalArgumentException();
                }
                Group group = groupsById.apply(fields[1]);
                if (group == null) {
                    group = new Group(fields[1], "");
                }
                List<IndividualTicket> composition = new ArrayList<>();
                for (String ticketId : fields[4].split(LIST_SEPARATOR)) {
                    IndividualTicket ticket = individual.get(ticketId);
                    if (ticket != null) {
                        composition.add(ticket);
                    }
                }
                groupTickets.add(new GroupTicket(fields[0], group, LocalDate.parse(fields[2]),
                        new BigDecimal(fields[3]), composition));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                TextStore.warnMalformed(groupTicketsFile, fields);
            }
        }
        return new Loaded(new ArrayList<>(individual.values()), groupTickets);
    }

    @Override
    public void saveAll(Collection<IndividualTicket> individualTickets, Collection<GroupTicket> groupTickets) {
        TextStore.writeLines(ticketsFile, individualTickets.stream()
                .map(ticket -> String.join(TextStore.SEPARATOR, ticket.getId(), ticket.getUser().getDni(),
                        ticket.getDate().toString(), ticket.getPrice().toPlainString()))
                .toList());
        TextStore.writeLines(groupTicketsFile, groupTickets.stream()
                .map(ticket -> String.join(TextStore.SEPARATOR, ticket.getId(), ticket.getGroup().getId(),
                        ticket.getDate().toString(), ticket.getPrice().toPlainString(),
                        String.join(LIST_SEPARATOR, ticket.getTickets().stream().map(Ticket::getId).toList())))
                .toList());
    }
}
