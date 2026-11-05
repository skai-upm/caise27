package es.upm.amusementpark.repository;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.service.OperationException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores every ticket of the park in a single identifier space shared by all ticket types.
 */
public class TicketRepository {

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    /** Common uniqueness check for any ticket type; throws if any identifier is already used. */
    public void ensureAvailable(Collection<String> identifiers) {
        for (String id : identifiers) {
            if (id.isEmpty()) {
                throw new OperationException("ticket identifier must not be empty");
            }
            if (tickets.containsKey(id)) {
                throw new OperationException("a ticket with identifier '" + id + "' already exists");
            }
        }
        if (identifiers.stream().distinct().count() != identifiers.size()) {
            throw new OperationException("duplicate ticket identifiers");
        }
    }

    public void add(Ticket ticket) {
        ensureAvailable(List.of(ticket.getIdentifier()));
        tickets.put(ticket.getIdentifier(), ticket);
    }

    public void remove(Ticket ticket) {
        tickets.remove(ticket.getIdentifier());
    }

    public Optional<Ticket> findById(String identifier) {
        return Optional.ofNullable(tickets.get(identifier));
    }

    public List<Ticket> findAll() {
        return List.copyOf(tickets.values());
    }

    /** Individual tickets of a user, found by traversing all tickets. */
    public List<IndividualTicket> individualTicketsOf(User user) {
        return tickets.values().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .filter(t -> t.getUser() == user)
                .toList();
    }

    /** Group tickets purchased by a group, found by traversal. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        return groupTickets().stream().filter(t -> t.getGroup() == group).toList();
    }

    /** A group is locked while it has an active group ticket. */
    public boolean isLocked(Group group, LocalDate today) {
        return groupTicketsOf(group).stream().anyMatch(t -> t.isActiveOn(today));
    }

    /** The group ticket whose composition contains the given ticket, if any. */
    public Optional<GroupTicket> containerOf(IndividualTicket ticket) {
        return groupTickets().stream().filter(g -> g.contains(ticket)).findFirst();
    }

    public List<GroupTicket> groupTickets() {
        return tickets.values().stream().filter(GroupTicket.class::isInstance).map(GroupTicket.class::cast).toList();
    }
}
