package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction of every park ticket: an identifier, a date of use and a price.
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
     * True if the ticket is for {@code today} or a later day.
     */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * True if the ticket is for a day strictly after {@code today}.
     */
    public boolean isFuture(LocalDate today) {
        return date.isAfter(today);
    }
}
