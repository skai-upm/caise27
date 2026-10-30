package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for individual and group tickets.
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

    /** Users admitted by this ticket. */
    public abstract List<User> getUsers();

    /** Price paid for this ticket at purchase time. */
    public abstract BigDecimal getPrice();

    /** A ticket dated on or after {@code today} is current or future. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket can be used to enter attractions only on its date. */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** A ticket can be deleted (refunded) only when its date is strictly later than {@code today}. */
    public boolean isRefundableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
