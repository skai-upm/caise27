package es.upm.etsisi.park.business.model.ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.park.business.model.user.User;

/**
 * Common abstraction for every ticket type: an identifier from a shared
 * identifier space, a date of use, a price and the users it admits. Every
 * ticket offers general access; subclasses add their specific capability.
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

    public LocalDate getDate() {
        return date;
    }

    /** Active tickets are dated today or later; the others are historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket can only be used on its own date. */
    @Override
    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    /** Only tickets dated strictly after today may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    public boolean admits(User user) {
        return getHolders().contains(user);
    }

    public String getFormattedPrice() {
        return Money.format(getPrice());
    }

    /** Final price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** Users who enter an attraction with this ticket. */
    @Override
    public abstract List<User> getHolders();
}
