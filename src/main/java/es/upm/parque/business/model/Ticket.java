package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for individual and group tickets.
 */
public abstract class Ticket implements AccessCredential {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String id, LocalDate date, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /**
     * True when the ticket is for the given day or a later one.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * True when the ticket is for a day strictly after the given one.
     */
    public boolean isFutureOn(LocalDate today) {
        return date.isAfter(today);
    }
}
