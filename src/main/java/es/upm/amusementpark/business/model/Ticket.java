package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every park ticket: an identifier, the single date on
 * which it can be used, a price and the users it admits.
 */
public abstract class Ticket implements GeneralAccess {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

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

    /** Price charged for the ticket, in euros with two decimals. */
    public abstract BigDecimal getPrice();

    /** Users admitted by this ticket. */
    @Override
    public abstract List<User> getHolders();

    /** Active tickets are dated today or later; older ones are historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket can only be used on its own date. */
    @Override
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** Only tickets dated strictly after today may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    public boolean isHeldBy(User user) {
        return getHolders().contains(user);
    }

    /** Applies a percentage discount, rounding half-up to cents. */
    protected static BigDecimal discounted(BigDecimal basePrice, int discountPercent) {
        return basePrice.multiply(BigDecimal.valueOf(100L - discountPercent))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
