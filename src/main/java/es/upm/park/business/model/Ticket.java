package es.upm.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Information and behavior shared by every ticket type: a unique identifier,
 * a date of use and a price obtained from a base price and a discount.
 * Whether a ticket is active is derived from its date and the current date.
 */
public abstract class Ticket implements Admission {

    private final String id;
    private final LocalDate date;
    private final BigDecimal basePrice;
    private final BigDecimal discount;

    protected Ticket(String id, LocalDate date, BigDecimal basePrice, BigDecimal discount) {
        this.id = id;
        this.date = date;
        this.basePrice = basePrice;
        this.discount = discount;
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

    /** Discount fraction applied to the base price (0 for none). */
    public BigDecimal getDiscount() {
        return discount;
    }

    /** Price charged: base price minus discount, in cents. */
    public BigDecimal getPrice() {
        return basePrice.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Active tickets are dated on or after the current date. */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    /** Only tickets dated strictly after the current date may be deleted. */
    public boolean isDeletable(LocalDate today) {
        return date.isAfter(today);
    }

    /** Identifiers this ticket occupies in the shared ticket identifier space. */
    public List<String> ids() {
        return List.of(id);
    }

    /** A ticket is valid for entry only on its own date. */
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** Users admitted to an attraction when this ticket is used. */
    public abstract List<User> holders();

    /** Whether this ticket concerns the given user. */
    public abstract boolean involves(User user);
}
