package com.parque;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Individual-ticket operations. The current date and price are always supplied by the controller, so the
 * service never consults the real clock. Rejected operations throw {@link IllegalArgumentException} and
 * leave memory and file unchanged.
 */
public class TicketService {
    private final TicketRepository repository;
    private final UserService users;
    private List<Ticket> tickets;

    public TicketService(TicketRepository repository, UserService users) throws IOException {
        this.repository = repository;
        this.users = users;
        this.tickets = new ArrayList<>(repository.load(users::findByDni));
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public Ticket find(String id) {
        for (Ticket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    /** Creates a ticket for a registered user and a date not earlier than {@code today}. */
    public Ticket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        require(UserValidator.isNonBlank(id), "Ticket identifier must not be empty");
        require(find(id) == null, "A ticket with identifier " + id + " already exists");
        User user = users.findByEmail(email);
        require(user != null, "No user with email " + email);
        require(!date.isBefore(today), "Ticket date " + date + " is before the system date " + today);
        Ticket ticket = new Ticket(id, user, date, price);
        List<Ticket> next = new ArrayList<>(tickets);
        next.add(ticket);
        commit(next);
        return ticket;
    }

    /** Deletes a ticket whose date is strictly later than {@code today}. */
    public void delete(String id, LocalDate today) {
        Ticket ticket = find(id);
        require(ticket != null, "No ticket with identifier " + id);
        require(ticket.getDate().isAfter(today),
                "Ticket " + id + " cannot be deleted: its date " + ticket.getDate() + " is not after " + today);
        List<Ticket> next = new ArrayList<>(tickets);
        next.remove(ticket);
        commit(next);
    }

    /** True if the user owns a ticket dated on or after {@code today} (ticketDate >= SYSTEM DATE). */
    public boolean hasCurrentOrFutureTicket(User user, LocalDate today) {
        for (Ticket t : tickets) {
            if (t.getUser() == user && !t.getDate().isBefore(today)) {
                return true;
            }
        }
        return false;
    }

    private void commit(List<Ticket> next) {
        try {
            repository.save(next);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save tickets: " + e.getMessage(), e);
        }
        tickets = next;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
