package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for every kind of ticket: an identifier, a date of use,
 * the price paid, and the users the ticket admits.
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


    /** A ticket is active (current or future) when its date is on or after the given day. */
    public boolean isActiveOn(LocalDate day) {
        return !date.isBefore(day);
    }

    /** A ticket can be refunded only when its date is strictly after the given day. */
    public boolean isRefundableOn(LocalDate day) {
        return date.isAfter(day);
    }
}
