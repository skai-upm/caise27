package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all tickets. Every ticket type shares this identifier space.
 */
public class TicketRepository {

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    public boolean exists(String identifier) {
        return tickets.containsKey(identifier);
    }

    public void save(Ticket ticket) {
        tickets.put(ticket.getIdentifier(), ticket);
    }

    public Optional<Ticket> findById(String identifier) {
        return Optional.ofNullable(tickets.get(identifier));
    }

    public boolean delete(String identifier) {
        return tickets.remove(identifier) != null;
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    /** Individual tickets bought for the user, found by traversing all tickets. */
    public List<IndividualTicket> findIndividualTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            }
        }
        return result;
    }
}
