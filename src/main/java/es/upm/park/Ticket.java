package es.upm.park;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction of every ticket: an identifier, a date of use and a price.
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

    /** True if the ticket is dated on or after {@code day} (current or future). */
    public boolean isOnOrAfter(LocalDate day) {
        return !date.isBefore(day);
    }

    /** Price paid for this ticket. */
    public abstract BigDecimal getPrice();
}
