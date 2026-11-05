package es.upm.amusementpark.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Information and behavior common to every ticket type: identifier, date of use and price.
 */
public abstract class Ticket implements GeneralAccess {

    private final String id;
    private final LocalDate date;

    protected Ticket(String id, LocalDate date) {
        this.id = Objects.requireNonNull(id, "id");
        this.date = Objects.requireNonNull(date, "date");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    /** @return the price charged for this ticket */
    public abstract BigDecimal getPrice();

    /** A ticket is active when its date is equal to or later than {@code today}. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket can be deleted only when its date is strictly later than {@code today}. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    /** @return e-mails of the users admitted together when this ticket is used */
    @Override
    public abstract List<String> holders();

    /** @return whether this ticket is associated with the user identified by {@code email} */
    public abstract boolean concernsUser(String email);
}
