package es.upm.etsisi.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identity, date of use, price and the users
 * it admits.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

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

    public boolean isValidOn(LocalDate today) {
        return date.isEqual(today);
    }

    public abstract BigDecimal getPrice();

    /** Users admitted by this ticket. */
    public abstract List<User> getUsers();
}
