package es.upm.amusementpark.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Common abstraction of every ticket type: unique identifier, date of use and price,
 * plus the rules that depend on the current system date.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal basePrice;
    private final BigDecimal discountRate;

    protected Ticket(String id, LocalDate date, BigDecimal basePrice) {
        this(id, date, basePrice, BigDecimal.ZERO);
    }

    protected Ticket(String id, LocalDate date, BigDecimal basePrice, BigDecimal discountRate) {
        this.id = id;
        this.date = date;
        this.basePrice = basePrice;
        this.discountRate = discountRate;
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

    /** Discount applied to the base price, as a fraction (0.10 = 10%). */
    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    /** Price charged for this ticket: base price minus its discount, rounded to cents. */
    public BigDecimal getPrice() {
        return basePrice.multiply(BigDecimal.ONE.subtract(discountRate)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Whether this ticket occupies the given identifier in the shared identifier space. */
    public boolean usesId(String ticketId) {
        return id.equals(ticketId);
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
