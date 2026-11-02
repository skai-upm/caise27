package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for every ticket type: identifier, date of use and price.
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

    public LocalDate getDate() {
        return date;
    }

    /** A ticket is active when its date is today or later; otherwise it is historical. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket can be used only on its own date. */
    @Override
    public boolean isValidOn(LocalDate today) {
        return date.equals(today);
    }

    /** Only tickets dated strictly after today may be deleted. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    /** Total price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** Users admitted with this ticket. */
    public List<User> getHolders() {
        return getEntryTickets().stream().map(IndividualTicket::getUser).toList();
    }

    /** Shared price rule: a percentage discount over a base price. */
    protected static BigDecimal applyDiscount(BigDecimal basePrice, int discountPercent) {
        return Money.discounted(basePrice, discountPercent);
    }
}
