package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction for every ticket type: identifier, date of use and price behaviour.
 */
public abstract class Ticket implements GeneralAccess {

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

    /** Price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** A ticket is active on its date and before it; afterwards it is historical. */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after today may be deleted. */
    public boolean isDeletable(LocalDate today) {
        return date.isAfter(today);
    }

    @Override
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }
}
