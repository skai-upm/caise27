package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for every ticket type: a unique identifier, the single
 * date on which it is valid, and the price fixed when it was created.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String id, LocalDate date, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** A ticket is active on its date and before it; afterwards it is historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after the given day may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
