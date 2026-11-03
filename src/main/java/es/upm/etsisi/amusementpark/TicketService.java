package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Individual ticket operations. Every date check uses the system date supplied by the controller.
 * Persists the tickets after every successful change. Rule violations are reported with
 * {@link IllegalArgumentException}.
 */
public class TicketService {

    private final TicketFileStorage storage;
    private final UserService users;
    private final List<Ticket> tickets;

    public TicketService(TicketFileStorage storage, UserService users) {
        this.storage = storage;
        this.users = users;
        this.tickets = new ArrayList<>(storage.load(users.getUsers()));
    }

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    public Optional<Ticket> find(String id) {
        return tickets.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    /** Buys a ticket for a registered user, for a date on or after {@code today}, at {@code price}. */
    public Ticket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        require(UserValidator.isNotBlank(id), "Ticket identifier must not be empty");
        require(find(id).isEmpty(), "A ticket with identifier " + id + " already exists");
        User user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No registered user with email " + email));
        require(!date.isBefore(today), "Ticket date " + date + " is before the system date " + today);
        Ticket ticket = new Ticket(id, user, date, price);
        tickets.add(ticket);
        saveOrRevert(() -> tickets.remove(ticket));
        return ticket;
    }

    /** Deletes a ticket whose date is strictly later than {@code today}. */
    public void delete(String id, LocalDate today) {
        Ticket ticket = find(id).orElseThrow(() -> new IllegalArgumentException("No ticket with identifier " + id));
        require(ticket.getDate().isAfter(today),
                "Ticket " + id + " cannot be deleted: its date " + ticket.getDate() + " is not after " + today);
        int index = tickets.indexOf(ticket);
        tickets.remove(index);
        saveOrRevert(() -> tickets.add(index, ticket));
    }

    /** True if the user owns a ticket dated on or after {@code today}. */
    public boolean hasActiveTickets(User user, LocalDate today) {
        return tickets.stream().anyMatch(t -> t.belongsTo(user) && !t.getDate().isBefore(today));
    }

    private void saveOrRevert(Runnable revert) {
        try {
            storage.save(tickets);
        } catch (RuntimeException e) {
            revert.run();
            throw e;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
