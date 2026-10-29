package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identifier, date of use and price information.
 */
public abstract class Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final String id;
    private final LocalDate date;
    private final BigDecimal basePrice;
    private final int discountPercent;

    protected Ticket(String id, LocalDate date, BigDecimal basePrice, int discountPercent) {
        this.id = id;
        this.date = date;
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Base price with this ticket's discount applied, rounded to cents.
     */
    protected BigDecimal unitPrice() {
        return basePrice.multiply(BigDecimal.valueOf(100L - discountPercent))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * Active tickets are dated today or later; earlier tickets are historical.
     */
    public boolean isActive(LocalDate today) {
        return !date.isBefore(today);
    }

    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    public abstract BigDecimal getPrice();

    /**
     * Users admitted by this ticket.
     */
    public abstract List<User> getUsers();

    /**
     * Returns this ticket or a ticket it contains with the given identifier, or null.
     */
    public abstract Ticket findTicket(String ticketId);
}
