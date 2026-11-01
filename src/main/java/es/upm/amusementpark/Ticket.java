package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every kind of ticket: an identifier, a date of use,
 * the price paid, and the users the ticket admits.
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

    /** Users admitted by this ticket. */
    public abstract List<User> getUsers();

    /** A ticket is active (current or future) when its date is on or after the given day. */
    public boolean isActiveOn(LocalDate day) {
        return !date.isBefore(day);
    }

    /** A ticket can be refunded only when its date is strictly after the given day. */
    public boolean isRefundableOn(LocalDate day) {
        return date.isAfter(day);
    }
}
