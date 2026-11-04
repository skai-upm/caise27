package es.upm.park;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every kind of ticket: an identifier, a date of use and a price.
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

    /** Users admitted when this ticket is used (polymorphic: one user or all group members). */
    public abstract List<User> getHolders();
}
