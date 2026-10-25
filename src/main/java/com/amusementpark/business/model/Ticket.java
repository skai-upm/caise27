package com.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for individual and group tickets.
 */
public abstract class Ticket implements Admission {
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

    /**
     * Current or future with respect to the given date (date of use on or after it).
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * Strictly after the given date.
     */
    public boolean isFutureOn(LocalDate today) {
        return date.isAfter(today);
    }

    /**
     * Usable for entry on the given date.
     */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }
}
