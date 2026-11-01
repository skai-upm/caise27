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

    /**
     * A ticket is active (current or future) when its date is on or after the given date.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * A ticket can be refunded only when its date is strictly after the given date.
     */
    public boolean isRefundableOn(LocalDate today) {
        return date.isAfter(today);
    }

    /**
     * A ticket gives access only on its own date.
     */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }
}
