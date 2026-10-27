package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all tickets. Every ticket type shares this identifier space. When a
 * {@link TicketFileStorage} is given, tickets are loaded from it on creation
 * and saved to it after every change.
 */
public class TicketRepository {

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final TicketFileStorage storage;

    /** Creates a repository that keeps tickets in memory only. */
    public TicketRepository() {
        this.storage = null;
    }

    public TicketRepository(TicketFileStorage storage, UserRepository users, GroupRepository groups) {
        this.storage = storage;
        for (Ticket ticket : storage.load(users, groups)) {
            tickets.put(ticket.getIdentifier(), ticket);
        }
    }

    public boolean exists(String identifier) {
        return tickets.containsKey(identifier);
    }

    public void save(Ticket ticket) {
        tickets.put(ticket.getIdentifier(), ticket);
        persist();
    }

    public Optional<Ticket> findById(String identifier) {
        return Optional.ofNullable(tickets.get(identifier));
    }

    public boolean delete(String identifier) {
        boolean removed = tickets.remove(identifier) != null;
        persist();
        return removed;
    }

    /** Writes the current tickets to the storage, if any. */
    public void persist() {
        if (storage != null) {
            storage.save(tickets.values());
        }
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
