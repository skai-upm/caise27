package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Common abstraction for every ticket type: identifier, date of use and price. Implements the general access
 * capability, so any ticket can be handled where the concrete type does not matter.
 */
public abstract class Ticket implements AccessTicket {

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

    @Override
    public LocalDate getDate() {
        return date;
    }

    /**
     * Active tickets are dated on or after the given current date; the rest are historical.
     */
    public boolean isActiveOn(LocalDate current) {
        return !date.isBefore(current);
    }

    /**
     * Only tickets dated strictly after the current date may be deleted.
     */
    public boolean isDeletableOn(LocalDate current) {
        return date.isAfter(current);
    }

    /**
     * A ticket can only be used on its own date.
     */
    @Override
    public boolean isValidOn(LocalDate current) {
        return date.equals(current);
    }

    public abstract BigDecimal getPrice();

    protected static BigDecimal discounted(BigDecimal basePrice, int discountPercent) {
        return basePrice.multiply(BigDecimal.valueOf(100 - discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
