package com.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for individual and group tickets: an identifier, a date of use and the price paid.
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

    /** The users this ticket admits. */
    public abstract List<User> getHolders();

    /** Dated today or later: the ticket still blocks deletions and group changes. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Strictly in the future: the ticket may still be deleted. */
    public boolean isRefundableOn(LocalDate today) {
        return date.isAfter(today);
    }

    /** Admits its holders to attractions only on its own date. */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }
}
