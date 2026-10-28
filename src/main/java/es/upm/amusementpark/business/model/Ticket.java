package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction of every ticket type: a unique identifier, a date of use and a price.
 */
public abstract class Ticket implements AccessTicket {

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

    /**
     * Price charged for this ticket.
     */
    public abstract BigDecimal getPrice();

    /**
     * Individual tickets this ticket consists of: itself for an individual ticket, its composition
     * for a group ticket.
     */
    public abstract List<IndividualTicket> individualTickets();

    /**
     * A ticket is active on or before its date of use; afterwards it is historical.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * Only tickets dated strictly after the current date may be deleted.
     */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
