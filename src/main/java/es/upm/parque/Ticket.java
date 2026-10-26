package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An individual admission ticket for one user and one date, with the price paid.
 */
public class Ticket {
    private final String id;
    private final LocalDate date;
    private final User user;
    private final BigDecimal price;

    public Ticket(String id, LocalDate date, User user, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.user = user;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** True when the ticket is for {@code today} or a later date. */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /** True when the ticket is for a date strictly after {@code today}. */
    public boolean isFuture(LocalDate today) {
        return date.isAfter(today);
    }

    @Override
    public String toString() {
        return id + " (" + date + ", " + user.getDni() + ", " + price.toPlainString() + ")";
    }
}
