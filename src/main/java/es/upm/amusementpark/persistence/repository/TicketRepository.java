package es.upm.amusementpark.persistence.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.persistence.TicketFileStore;

/**
 * Stores every ticket of the park in one shared identifier space.
 */
public class TicketRepository {

    private final Map<String, Ticket> ticketsById = new LinkedHashMap<>();
    private final TicketFileStore store;

    /** In-memory repository without persistence. */
    public TicketRepository() {
        this.store = null;
    }

    public TicketRepository(TicketFileStore store, UserRepository users, GroupRepository groups) {
        this.store = store;
        for (Ticket ticket : store.load(users, groups)) {
            ticketsById.put(ticket.getId(), ticket);
        }
    }

    /** Persists the current tickets. */
    public void save() {
        if (store != null) {
            store.save(ticketsById.values());
        }
    }

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
        save();
    }

    public Ticket findById(String id) {
        return ticketsById.get(id);
    }

    public void remove(Ticket ticket) {
        ticketsById.remove(ticket.getId());
        save();
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(ticketsById.values());
    }
}
