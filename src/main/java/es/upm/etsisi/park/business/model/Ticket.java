package es.upm.etsisi.park.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Common abstraction for every ticket type: identity, date of use and price behaviour.
 */
public abstract class Ticket implements AccessTicket {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final String id;
    private final LocalDate date;
    private final BigDecimal basePrice;

    protected Ticket(String id, LocalDate date, BigDecimal basePrice) {
        this.id = id;
        this.date = date;
        this.basePrice = basePrice;
    }

    @Override
    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Final price charged for this ticket.
     */
    public abstract BigDecimal getPrice();

    /**
     * Users that this ticket lets into an attraction.
     */
    @Override
    public abstract List<User> getHolders();

    /**
     * Every identifier this ticket occupies in the shared ticket identifier space.
     */
    public List<String> getIdentifiers() {
        return List.of(id);
    }

    /**
     * Finds this ticket, or a ticket it is composed of, by identifier.
     */
    public Optional<Ticket> findTicket(String identifier) {
        return id.equals(identifier) ? Optional.of(this) : Optional.empty();
    }

    /**
     * A ticket is active on its date and before it; afterwards it is historical.
     */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    @Override
    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }

    /**
     * Only tickets dated strictly after the current date may be deleted.
     */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    public boolean involves(User user) {
        return getHolders().contains(user);
    }

    protected static BigDecimal discounted(BigDecimal base, int discountPercent) {
        BigDecimal factor = ONE_HUNDRED.subtract(BigDecimal.valueOf(discountPercent));
        return base.multiply(factor).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
