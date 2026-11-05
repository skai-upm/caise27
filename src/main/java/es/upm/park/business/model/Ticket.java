package es.upm.park.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every kind of ticket: an identifier, a date of use and a price.
 */
public abstract class Ticket implements AccessCredential {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String id, LocalDate date, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** Users admitted when this ticket is used (polymorphic: one user or all group members). */
    @Override
    public abstract List<User> getHolders();
}
