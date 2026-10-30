package es.upm.etsisi.amusementpark.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Behaviour shared by every kind of ticket: identity, date of use, price and the users it admits.
 */
public abstract class Ticket {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

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

    public abstract BigDecimal getPrice();

    /**
     * Users admitted by this ticket.
     */
    public abstract List<User> getHolders();

    /**
     * Individual tickets this ticket consists of (itself for an individual ticket).
     */
    public abstract List<IndividualTicket> getIndividualTickets();

    /** Active tickets are dated today or later; the rest are historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    public boolean involves(User user) {
        return getHolders().contains(user);
    }

    protected static BigDecimal discounted(BigDecimal basePrice, int discountPercent) {
        return basePrice.multiply(HUNDRED.subtract(BigDecimal.valueOf(discountPercent)))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
