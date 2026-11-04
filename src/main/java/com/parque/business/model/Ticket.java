package com.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction of every ticket: an identifier (unique across all kinds of tickets), a date of use
 * and a price. Specialised by {@link IndividualTicket} and {@link GroupTicket}.
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

    /**
     * Users admitted with this ticket. Lets attractions treat every kind of ticket uniformly.
     */
    public abstract List<User> getVisitors();

    /** Price paid for this ticket. */
    public abstract BigDecimal getPrice();

    /** Price with two decimals, e.g. {@code 40.00}. */
    public String getPriceText() {
        return getPrice().toPlainString();
    }

    /** True if the date of use is on or after {@code today} (ticketDate >= SYSTEM DATE). */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /** True if the date of use is strictly after {@code today}. */
    public boolean isFuture(LocalDate today) {
        return date.isAfter(today);
    }
}
