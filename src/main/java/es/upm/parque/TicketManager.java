package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps all tickets (current and historical) and applies the ticket rules. Date rules receive the
 * system date from the controller; this class never reads the computer clock.
 */
public class TicketManager {

    private final Map<String, Ticket> ticketsById = new LinkedHashMap<>();
    private final UserManager users;
    private final TicketStorage storage;

    public TicketManager(UserManager users, TicketStorage storage) {
        this.users = users;
        this.storage = storage;
        for (Ticket t : storage.load(users::findByDni)) {
            ticketsById.put(t.getId(), t);
        }
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(ticketsById.values());
    }

    public Ticket find(String id) {
        return ticketsById.get(id);
    }

    public Ticket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        if (!UserValidator.isNotBlank(id)) {
            throw new IllegalArgumentException("The ticket identifier cannot be empty");
        }
        if (ticketsById.containsKey(id)) {
            throw new IllegalArgumentException("A ticket with identifier " + id + " already exists");
        }
        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Cannot buy a ticket for a past date (" + date + ")");
        }
        Ticket ticket = new Ticket(id, date, price, user);
        ticketsById.put(id, ticket);
        persist();
        return ticket;
    }

    public void delete(String id, LocalDate today) {
        Ticket ticket = ticketsById.get(id);
        if (ticket == null) {
            throw new IllegalArgumentException("No ticket with identifier " + id);
        }
        if (!ticket.getDate().isAfter(today)) {
            throw new IllegalArgumentException("Ticket " + id + " for " + ticket.getDate()
                + " cannot be deleted: only tickets dated after " + today + " can be deleted");
        }
        ticketsById.remove(id);
        persist();
    }

    /** @return true if the user owns a ticket dated on or after {@code today} */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        for (Ticket t : ticketsById.values()) {
            if (t.isOwnedBy(user) && !t.getDate().isBefore(today)) {
                return true;
            }
        }
        return false;
    }

    /** Keeps the user's tickets as history once the user has been deleted. */
    public void detachOwner(User user) {
        for (Ticket t : ticketsById.values()) {
            if (t.isOwnedBy(user)) {
                t.detachOwner();
            }
        }
    }

    private void persist() {
        storage.save(new ArrayList<>(ticketsById.values()));
    }
}
