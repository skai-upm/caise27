package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for individual and group tickets: an identifier, a date of use and a price.
 */
public abstract class Ticket implements AccessCapability {

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
