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
     * True when the ticket is for the given day or a later one.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * True when the ticket is for a day strictly after the given one.
     */
    public boolean isFutureOn(LocalDate today) {
        return date.isAfter(today);
    }

    /**
     * Users admitted with this ticket.
     */
    public abstract List<User> getUsers();
}
