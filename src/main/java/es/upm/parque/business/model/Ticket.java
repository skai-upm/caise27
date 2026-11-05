package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: a unique identifier, a date of use and a price. Every ticket has the
 * general access capability; subclasses add the individual or group capability.
 */
public abstract class Ticket implements GeneralAccess {

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

    /** Users who enter an attraction with this ticket. */
    @Override
    public abstract List<User> getHolders();

    /** A ticket is active when its date is equal to or later than the given current date. */
    public boolean isActiveOn(LocalDate currentDate) {
        return !date.isBefore(currentDate);
    }

    /** A ticket may be deleted only when its date is strictly later than the given current date. */
    public boolean isDeletableOn(LocalDate currentDate) {
        return date.isAfter(currentDate);
    }
}
