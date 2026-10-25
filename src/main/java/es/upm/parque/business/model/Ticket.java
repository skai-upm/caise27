package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for individual and group tickets.
 */
public abstract class Ticket implements Access {

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
     * A ticket is active (current or future) when its date is on or after the given date.
     */
    public boolean isActiveOn(LocalDate currentDate) {
        return !date.isBefore(currentDate);
    }

    /**
     * Tickets can only be deleted when their date is strictly later than the given date.
     */
    public boolean isDeletableOn(LocalDate currentDate) {
        return date.isAfter(currentDate);
    }
}
