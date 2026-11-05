package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction of every park ticket: identifier, date of use and price.
 */
public abstract class Ticket {

    private final String identifier;
    private final LocalDate date;

    protected Ticket(String identifier, LocalDate date) {
        this.identifier = identifier;
        this.date = date;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Users this ticket admits into an attraction. */
    public abstract List<User> admittedUsers();

    /** Price charged for this ticket. */
    public abstract BigDecimal getPrice();

    /** Price formatted with two decimals followed by the currency. */
    public String formattedPrice() {
        return getPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " EUR";
    }

    /** A ticket is active when its date is equal to or later than the given current date. */
    public boolean isActiveOn(LocalDate currentDate) {
        return !date.isBefore(currentDate);
    }

    /** A ticket may be deleted only when its date is strictly later than the current date. */
    public boolean isDeletableOn(LocalDate currentDate) {
        return date.isAfter(currentDate);
    }

    /** Whether the ticket is valid on the given day. */
    public boolean isValidOn(LocalDate day) {
        return date.equals(day);
    }
}
