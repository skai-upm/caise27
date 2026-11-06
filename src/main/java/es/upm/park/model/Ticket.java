package es.upm.park.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: a unique identifier, the single
 * date on which it is valid, and the price fixed when it was created.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String id, LocalDate date, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    /** The price charged for this ticket. */
    public BigDecimal getPrice() {
        return price;
    }

    /** Common price rule: a base price reduced by a percentage, rounded to cents. */
    protected static BigDecimal discounted(BigDecimal base, int percent) {
        BigDecimal factor = BigDecimal.valueOf(100L - percent).movePointLeft(2);
        return base.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /** A ticket is active on its date and before it; afterwards it is historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket is valid for access only on its own date. */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** Emails of the users this ticket admits to an attraction. */
    public abstract List<String> admittedUsers();

    /** Only tickets dated strictly after the given day may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }
}
