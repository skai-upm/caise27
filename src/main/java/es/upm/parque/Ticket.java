package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for park tickets: an identifier, a date of use, a price and the users it admits.
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

    public abstract BigDecimal getPrice();

    /**
     * The users admitted by this ticket.
     */
    public abstract List<User> getVisitors();

    /**
     * A ticket dated today or later is still current.
     */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * Only tickets dated strictly after today can be refunded.
     */
    public boolean isRefundable(LocalDate today) {
        return date.isAfter(today);
    }
}
