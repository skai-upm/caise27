package es.upm.etsisi.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identifier, date of use and price.
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

    /**
     * The individual tickets this ticket consists of (itself for an individual ticket).
     */
    public abstract List<IndividualTicket> getIndividualTickets();

    /** A ticket is active when its date is equal to or later than {@code today}. */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after {@code today} may be deleted. */
    public boolean isDeletable(LocalDate today) {
        return date.isAfter(today);
    }
}
