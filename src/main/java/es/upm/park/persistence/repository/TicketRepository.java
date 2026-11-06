package es.upm.park.persistence.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.TicketFileStore;

/**
 * Keeps every ticket of the park, whatever its type, in one shared
 * identifier space, optionally backed by a file store loaded on creation.
 */
public class TicketRepository {

    private final List<Ticket> tickets = new ArrayList<>();
    private final TicketFileStore store;

    public TicketRepository() {
        this.store = null;
    }

    /** Loads stored tickets, linking existing users and groups. */
    public TicketRepository(TicketFileStore store, Function<String, Optional<User>> userByDni,
            Function<String, Optional<Group>> groupById) {
        this.store = store;
        tickets.addAll(store.load(userByDni, groupById));
    }

    /** The single identifier check used for all ticket types. */
    public boolean existsId(String id) {
        return tickets.stream().anyMatch(t -> t.ids().contains(id));
    }

    /** Adds a ticket, writing the file first when persistent. */
    public void add(Ticket ticket) {
        List<Ticket> snapshot = new ArrayList<>(tickets);
        snapshot.add(ticket);
        persist(snapshot);
        tickets.add(ticket);
    }

    /** Removes a ticket, writing the file first when persistent. */
    public void remove(Ticket ticket) {
        List<Ticket> snapshot = new ArrayList<>(tickets);
        snapshot.remove(ticket);
        persist(snapshot);
        tickets.remove(ticket);
    }

    /** Writes the current tickets after an in-place change (detached group). */
    public void save() {
        persist(tickets);
    }

    public Optional<Ticket> findById(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<Ticket> findAll() {
        return Collections.unmodifiableList(tickets);
    }

    private void persist(List<Ticket> snapshot) {
        if (store != null) {
            store.save(snapshot);
        }
    }
}
