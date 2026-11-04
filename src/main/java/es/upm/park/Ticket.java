package es.upm.park;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An individual ticket for a date of use, owned by a registered user, with the price captured
 * at purchase time.
 */
public class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;
    private final User user;

    public Ticket(String id, LocalDate date, BigDecimal price, User user) {
        this.id = id;
        this.date = date;
        this.price = price;
        this.user = user;
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

    public User getUser() {
        return user;
    }
}
