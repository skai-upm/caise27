package es.upm.parque.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Stores every ticket of every type in one shared identifier space.
 */
public class TicketRepository {

    private final Map<String, Ticket> ticketsById = new LinkedHashMap<>();

    /** The single uniqueness check for all ticket types. */
    public boolean exists(String id) {
        return ticketsById.containsKey(id);
    }

    public void add(Ticket ticket) {
        ticketsById.put(ticket.getId(), ticket);
    }

    public Ticket findById(String id) {
        return ticketsById.get(id);
    }

    public void remove(String id) {
        ticketsById.remove(id);
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(ticketsById.values());
    }

    /** Individual tickets of the user, found by traversing the stored tickets. */
    public List<IndividualTicket> individualTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : ticketsById.values()) {
            if (ticket instanceof IndividualTicket individual && individual.belongsTo(user)) {
                result.add(individual);
            }
        }
        return result;
    }
}
