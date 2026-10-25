package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for individual and group tickets: an identifier, a date of use, a price and the users it
 * gives access to.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public abstract BigDecimal getPrice();

    /**
     * The users this ticket gives access to.
     */
    public abstract List<User> getUsers();

    /**
     * True if the ticket is dated on or after the given day.
     */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * True if the ticket is dated strictly after the given day.
     */
    public boolean isFuture(LocalDate today) {
        return date.isAfter(today);
    }

    /**
     * True if the ticket can be used on the given day.
     */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }
}
