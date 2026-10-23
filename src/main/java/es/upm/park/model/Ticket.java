package es.upm.park.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identity, date of use, price
 * and the users it admits. Date rules are shared by all ticket types.
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

    public abstract BigDecimal getPrice();

    /**
     * Users admitted by this ticket.
     */
    public abstract List<User> getHolders();

    /**
     * A ticket is active on its date and on every earlier day; afterwards it
     * is historical.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * Only tickets dated strictly after today may be deleted.
     */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    /**
     * A ticket grants access only on its own date.
     */
    public boolean isValidOn(LocalDate today) {
        return date.isEqual(today);
    }

    public boolean involves(User user) {
        return getHolders().contains(user);
    }
}
