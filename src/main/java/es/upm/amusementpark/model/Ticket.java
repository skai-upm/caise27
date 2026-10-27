package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Common abstraction of every ticket type: unique identifier, date of use and price,
 * plus the rules that depend on the current system date.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal basePrice;

    protected Ticket(String id, LocalDate date, BigDecimal basePrice) {
        this.id = id;
        this.date = date;
        this.basePrice = basePrice;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Base price in force when the ticket was purchased. */
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /** Price charged for this ticket. */
    public BigDecimal getPrice() {
        return basePrice;
    }

    /** Active tickets are dated today or later; earlier ones are historical. */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after today may be deleted. */
    public boolean isDeletable(LocalDate today) {
        return date.isAfter(today);
    }

    /** Whether this ticket was purchased for the given user. */
    public abstract boolean involves(User user);
}
