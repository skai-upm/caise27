package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Keeps every ticket (individual, internal to a group ticket, and group tickets) in a single
 * identifier space. Time-dependent rules receive the current system date as a parameter.
 */
public class TicketManager {

    private final TicketRepository repository;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    public TicketManager(TicketRepository repository, Function<String, User> userByDni,
                         Function<String, Group> groupById) {
        this.repository = repository;
        for (Ticket t : repository.load(userByDni, groupById)) {
            tickets.put(t.getId(), t);
        }
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets.values());
    }

    public Ticket get(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket not found: " + id);
        }
        return ticket;
    }

    public IndividualTicket createIndividual(String id, User user, LocalDate date, BigDecimal price,
                                             LocalDate today) {
        requireNewTicket(id, date, today);
        IndividualTicket ticket = new IndividualTicket(id, date, price, user, user.getDni(), null);
        tickets.put(id, ticket);
        save();
        return ticket;
    }

    public GroupTicket createGroupTicket(String id, Group group, LocalDate date, BigDecimal unitPrice,
                                         LocalDate today) {
        requireNewTicket(id, date, today);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new IllegalArgumentException("Group " + group.getId() + " has no members");
        }
        GroupTicket groupTicket = new GroupTicket(id, date, GroupTicket.totalFor(unitPrice, members.size()),
                group, group.getId());
        tickets.put(id, groupTicket);
        int n = 0;
        for (User member : members) {
            String internalId;
            do {
                n++;
                internalId = id + "-" + n;
            } while (tickets.containsKey(internalId));
            IndividualTicket internal = new IndividualTicket(internalId, date, unitPrice, member,
                    member.getDni(), groupTicket);
            groupTicket.addTicket(internal);
            tickets.put(internalId, internal);
        }
        save();
        return groupTicket;
    }

    public void deleteIndividual(String id, LocalDate today) {
        if (!(get(id) instanceof IndividualTicket ticket)) {
            throw new IllegalArgumentException(id + " is a group ticket: use GROUPTICKET DELETE");
        }
        if (ticket.isPartOfGroupTicket()) {
            throw new IllegalArgumentException(id + " belongs to group ticket " + ticket.getGroupTicket().getId()
                    + ": use GROUPTICKET DELETE");
        }
        requireRefundable(ticket, today);
        tickets.remove(id);
        save();
    }

    public void deleteGroupTicket(String id, LocalDate today) {
        if (!(get(id) instanceof GroupTicket groupTicket)) {
            throw new IllegalArgumentException(id + " is not a group ticket: use TICKET DELETE");
        }
        requireRefundable(groupTicket, today);
        for (IndividualTicket internal : groupTicket.getTickets()) {
            tickets.remove(internal.getId());
        }
        tickets.remove(id);
        save();
    }

    /**
     * True if the user owns an individual ticket (bought directly or through a group) dated today or later.
     */
    public boolean hasCurrentOrFutureTicket(User user, LocalDate today) {
        return tickets.values().stream()
                .anyMatch(t -> t instanceof IndividualTicket i && i.getUser() == user && i.isCurrentOrFuture(today));
    }

    /**
     * True if the group has a group ticket dated today or later.
     */
    public boolean isGroupLocked(Group group, LocalDate today) {
        return tickets.values().stream()
                .anyMatch(t -> t instanceof GroupTicket g && g.getGroup() == group && g.isCurrentOrFuture(today));
    }

    private void requireNewTicket(String id, LocalDate date, LocalDate today) {
        Identifiers.require(id, "ticket");
        if (tickets.containsKey(id)) {
            throw new IllegalArgumentException("Ticket already exists: " + id);
        }
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Ticket date " + date + " is before the system date " + today);
        }
    }

    private static void requireRefundable(Ticket ticket, LocalDate today) {
        if (!ticket.isRefundable(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " dated " + ticket.getDate()
                    + " can only be deleted before that date (system date " + today + ")");
        }
    }

    private void save() {
        repository.save(tickets.values());
    }
}
