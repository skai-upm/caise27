package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for individual tickets. Dates are always checked against the
 * system date passed in by the controller. Every successful change is saved;
 * if saving fails the previous state is restored.
 */
public class TicketManager {

    private final TicketFileRepository repository;
    private final UserManager users;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    public TicketManager(TicketFileRepository repository, UserManager users) throws IOException {
        this.repository = repository;
        this.users = users;
        Map<String, User> byDni = new HashMap<>();
        for (User u : users.getUsers()) {
            byDni.put(u.getDni(), u);
        }
        for (Ticket t : repository.load(byDni::get)) {
            tickets.put(t.getId(), t);
        }
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    public Optional<Ticket> find(String id) {
        return Optional.ofNullable(tickets.get(id));
    }

    /** Creates a ticket for the user with that email on a date not before {@code today}. */
    public Ticket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price)
            throws UserException {
        if (!UserValidator.isNotBlank(id)) {
            throw new UserException("The ticket id must not be empty");
        }
        if (tickets.containsKey(id)) {
            throw new UserException("A ticket with id " + id + " already exists");
        }
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UserException("No user with email " + email));
        if (date.isBefore(today)) {
            throw new UserException("Cannot buy a ticket for a past date (" + date + " is before " + today + ")");
        }
        Ticket ticket = new Ticket(id, date, user, price);
        tickets.put(id, ticket);
        save(() -> tickets.remove(id));
        return ticket;
    }

    /** Deletes a ticket whose date is later than {@code today}. */
    public void delete(String id, LocalDate today) throws UserException {
        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            throw new UserException("No ticket with id " + id);
        }
        if (!ticket.getDate().isAfter(today)) {
            throw new UserException("Ticket " + id + " is for " + ticket.getDate()
                    + " and can only be deleted before that date");
        }
        Map<String, Ticket> previous = new LinkedHashMap<>(tickets);
        tickets.remove(id);
        save(() -> {
            tickets.clear();
            tickets.putAll(previous);
        });
    }

    /** True if the user owns a ticket dated on or after {@code today}. */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        return tickets.values().stream()
                .anyMatch(t -> t.belongsTo(user) && !t.getDate().isBefore(today));
    }

    /** Keeps the user's tickets as history without a reference to the deleted user. */
    public void detachUser(User user) {
        tickets.values().stream().filter(t -> t.belongsTo(user)).forEach(Ticket::detachUser);
    }

    private void save(Runnable rollback) throws UserException {
        try {
            repository.save(tickets.values());
        } catch (IOException e) {
            rollback.run();
            throw new UserException("Could not save tickets: " + e.getMessage());
        }
    }
}
