package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for park tickets: an identifier, a date of use, a price and the users it admits.
 */
public abstract class Ticket {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    /**
     * Parses a positive amount with two decimals, or returns null when the text is not valid.
     */
    public static BigDecimal parsePrice(String text) {
        try {
            BigDecimal value = new BigDecimal(text);
            return value.signum() > 0 ? value.setScale(2, RoundingMode.HALF_UP) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public abstract BigDecimal getPrice();

    /**
     * The users admitted by this ticket.
     */
    public abstract List<User> getVisitors();

    /**
     * A ticket dated today or later is still current.
     */
    public boolean isCurrentOrFuture(LocalDate today) {
        return !date.isBefore(today);
    }

    /**
     * Only tickets dated strictly after today can be refunded.
     */
    public boolean isRefundable(LocalDate today) {
        return date.isAfter(today);
    }
}
