package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type. All tickets share one identifier space.
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

    /** Price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** Users who enter an attraction with this ticket. */
    public abstract List<User> getHolders();

    /** A ticket is active on its date and on every earlier day; afterwards it is historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket is valid for entering attractions only on its own date. */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }
}
