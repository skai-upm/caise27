package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for individual and group tickets.
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

    /**
     * Users admitted by this ticket.
     */
    public abstract List<User> getUsers();

    /** The ticket can be used to enter attractions on this day. */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** The ticket date is today or later: it still imposes restrictions. */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /** The ticket date is strictly later than today: it can still be deleted. */
    public boolean isRefundable(LocalDate today) {
        return date.isAfter(today);
    }
}
