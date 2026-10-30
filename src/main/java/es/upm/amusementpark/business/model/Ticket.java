package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for individual and group tickets.
 */
public abstract class Ticket implements AccessTicket {

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


    /** Price paid for this ticket at purchase time. */
    public abstract BigDecimal getPrice();

    /** A ticket dated on or after {@code today} is current or future. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket can be used to enter attractions only on its date. */
    @Override
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** A ticket can be deleted (refunded) only when its date is strictly later than {@code today}. */
    public boolean isRefundableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
