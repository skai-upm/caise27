package es.upm.amusementpark.business.model.ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Common abstraction for every ticket type: identifier, date of use and price behaviour.
 */
public abstract class Ticket implements AccessTicket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int PRICE_SCALE = 2;

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

    /**
     * Price charged for this ticket.
     */
    public abstract BigDecimal getPrice();

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

    @Override
    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    /**
     * Applies a percentage to an amount, rounding half-up to cents. Shared by every price rule.
     */
    public static BigDecimal percentOf(BigDecimal amount, int percent) {
        return amount.multiply(BigDecimal.valueOf(percent)).divide(HUNDRED, PRICE_SCALE, RoundingMode.HALF_UP);
    }

    protected static BigDecimal applyDiscount(BigDecimal basePrice, int discountPercent) {
        return percentOf(basePrice, 100 - discountPercent);
    }
}
