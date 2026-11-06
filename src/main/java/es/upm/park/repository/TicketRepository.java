package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.model.Ticket;

/**
 * Keeps every ticket of the park, whatever its type, in one shared
 * identifier space.
 */
public class TicketRepository {

    private final List<Ticket> tickets = new ArrayList<>();

    /** The single identifier check used for all ticket types. */
    public boolean existsId(String id) {
        return tickets.stream().anyMatch(t -> t.ids().contains(id));
    }

    public void add(Ticket ticket) {
        tickets.add(ticket);
    }

    public void remove(Ticket ticket) {
        tickets.remove(ticket);
    }

    public Optional<Ticket> findById(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<Ticket> findAll() {
        return Collections.unmodifiableList(tickets);
    }
}
