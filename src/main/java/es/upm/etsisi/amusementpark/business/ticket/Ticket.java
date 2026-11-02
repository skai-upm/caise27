package es.upm.etsisi.amusementpark.business.ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Behavior shared by every ticket type: identity, date of use, activity and price.
 */
public abstract class Ticket implements AccessTicket {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final String identifier;
    private final LocalDate date;

    protected Ticket(String identifier, LocalDate date) {
        this.identifier = identifier;
        this.date = date;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * A ticket is active on its date and every earlier day; afterwards it is historical.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    @Override
    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    public abstract BigDecimal getPrice();

    @Override
    public abstract List<User> getUsers();

    protected static BigDecimal discounted(BigDecimal basePrice, int discountPercent) {
        return basePrice.multiply(HUNDRED.subtract(BigDecimal.valueOf(discountPercent)))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
