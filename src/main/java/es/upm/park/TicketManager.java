package es.upm.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Individual ticket operations. All date checks use the date supplied by the caller
 * (the application's configured system date), never the computer clock.
 */
public class TicketManager {

    private final TicketFileStorage storage;
    private Map<String, Ticket> tickets = new LinkedHashMap<>();

    public TicketManager(TicketFileStorage storage, UserManager users) {
        this.storage = storage;
        for (Ticket t : storage.load(users::findByDni)) {
            tickets.put(t.getId(), t);
        }
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    public Ticket find(String id) {
        return tickets.get(id);
    }

    /** Creates a ticket for {@code owner} dated {@code date} (not before {@code today}). */
    public Ticket create(String id, User owner, LocalDate date, BigDecimal price, LocalDate today) {
        requireValid(UserValidator.isNonBlank(id), "invalid ticket identifier");
        requireValid(!tickets.containsKey(id), "a ticket with identifier " + id + " already exists");
        requireValid(!date.isBefore(today), "ticket date " + date + " is before the system date " + today);
        Ticket ticket = new Ticket(id, owner, date, price);
        Map<String, Ticket> next = new LinkedHashMap<>(tickets);
        next.put(id, ticket);
        commit(next);
        return ticket;
    }

    /** Deletes a ticket whose date is strictly later than {@code today}. */
    public void delete(String id, LocalDate today) {
        Ticket ticket = tickets.get(id);
        requireValid(ticket != null, "no ticket with identifier " + id);
        requireValid(ticket.getDate().isAfter(today),
                "ticket " + id + " can only be deleted if its date is later than the system date " + today);
        Map<String, Ticket> next = new LinkedHashMap<>(tickets);
        next.remove(id);
        commit(next);
    }

    /** True if {@code user} owns a ticket dated on or after {@code today}. */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        for (Ticket t : tickets.values()) {
            if (t.isOwnedBy(user) && !t.getDate().isBefore(today)) {
                return true;
            }
        }
        return false;
    }

    private void commit(Map<String, Ticket> next) {
        storage.save(next.values());
        tickets = next;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
