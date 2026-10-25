package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An individual ticket for one date, held by a registered user.
 */
public class Ticket {

    private final String id;
    private final LocalDate date;
    private final User holder;
    private final BigDecimal price;

    public Ticket(String id, LocalDate date, User holder, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.holder = holder;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getHolder() {
        return holder;
    }

    public BigDecimal getPrice() {
        return price;
    }

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
