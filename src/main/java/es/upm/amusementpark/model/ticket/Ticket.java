package es.upm.amusementpark.model.ticket;

import es.upm.amusementpark.model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identifier, date of use and price behaviour.
 */
public abstract class Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int PRICE_SCALE = 2;

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
     * Users admitted by this ticket.
     */
    public abstract List<User> getUsers();

    /**
     * A ticket is active on its date and before it; afterwards it is historical.
     */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * Only tickets dated strictly after today may be deleted.
     */
    public boolean isDeletable(LocalDate today) {
        return date.isAfter(today);
    }

    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    protected static BigDecimal applyDiscount(BigDecimal basePrice, int discountPercent) {
        return basePrice.multiply(BigDecimal.valueOf(100L - discountPercent))
                .divide(HUNDRED, PRICE_SCALE, RoundingMode.HALF_UP);
    }
}
