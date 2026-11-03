package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction of all ticket types: identifier, date of use and price.
 */
public abstract class Ticket {

    private final String identifier;
    private final LocalDate date;

    protected Ticket(String identifier, LocalDate date) {
        this.identifier = identifier;
        this.date = date;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Price charged for this ticket, in euros with two decimals. */
    public abstract BigDecimal getPrice();

    /** Users admitted to an attraction when this ticket is used. */
    public abstract List<User> getHolders();

    /** Active tickets are dated today or later; earlier ones are historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket grants access only on its own date. */
    public boolean isValidOn(LocalDate today) {
        return date.isEqual(today);
    }

    /** Only tickets dated strictly after today may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
