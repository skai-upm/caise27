package es.upm.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction of every ticket type: a unique identifier, the date on
 * which the ticket can be used and its price.
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

    public abstract BigDecimal getPrice();

    /** Active tickets are dated on or after the given current date. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after the current date may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
