package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every kind of admission ticket: an identifier, a date of use, a price and the users it
 * admits.
 */
public abstract class Ticket implements AttractionAccess {
    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    public abstract BigDecimal getPrice();

    /** The users admitted with this ticket. */
    @Override
    public abstract List<User> getUsers();

    /** True when the ticket is for {@code today} or a later date. */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /** True when the ticket is for a date strictly after {@code today}. */
    public boolean isFuture(LocalDate today) {
        return date.isAfter(today);
    }

    @Override
    public String toString() {
        return id + " (" + date + ", " + getPrice().toPlainString() + ")";
    }
}
