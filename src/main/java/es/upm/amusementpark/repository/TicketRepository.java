package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.Ticket;

/**
 * Stores every ticket of the park in one shared identifier space.
 */
public class TicketRepository {

    private final Map<String, Ticket> ticketsById = new LinkedHashMap<>();

    /** Common identifier check for all ticket types, including group ticket components. */
    public boolean isIdAvailable(String id) {
        for (Ticket ticket : ticketsById.values()) {
            if (ticket.usesId(id)) {
                return false;
            }
        }
        return true;
    }

    /** The group ticket whose composition contains the given ticket identifier, or null. */
    public GroupTicket findGroupTicketContaining(String id) {
        for (Ticket ticket : ticketsById.values()) {
            if (ticket instanceof GroupTicket group && !group.getId().equals(id) && group.usesId(id)) {
                return group;
            }
        }
        return null;
    }

    public void add(Ticket ticket) {
        ticketsById.put(ticket.getId(), ticket);
    }

    public Ticket findById(String id) {
        return ticketsById.get(id);
    }

    public void remove(Ticket ticket) {
        ticketsById.remove(ticket.getId());
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(ticketsById.values());
    }
}
