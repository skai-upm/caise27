package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.model.access.Access;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identity, date of use, price and the users
 * it admits. Concrete tickets add the access capability they provide.
 */
public abstract class Ticket implements Access {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    /** Active tickets are those for today or a later date; older ones are historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets strictly in the future may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    @Override
    public boolean isValidOn(LocalDate today) {
        return date.isEqual(today);
    }

    public abstract BigDecimal getPrice();

    /** Users admitted by this ticket. */
    @Override
    public abstract List<User> getUsers();
}
