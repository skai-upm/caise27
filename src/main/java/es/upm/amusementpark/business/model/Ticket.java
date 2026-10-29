package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Information and behavior shared by every ticket type: identifier, date of
 * use and price.
 */
public abstract class Ticket {

    private final String identifier;
    private final LocalDate date;
    private final BigDecimal price;

    protected Ticket(String identifier, LocalDate date, BigDecimal price) {
        this.identifier = Objects.requireNonNull(identifier);
        this.date = Objects.requireNonNull(date);
        this.price = Objects.requireNonNull(price);
    }

    public String getIdentifier() {
        return identifier;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** A ticket is active when its date is the given day or later. */
    public boolean isActiveOn(LocalDate today) {
        return !date.isBefore(today);
    }

    /** A ticket may be deleted only when its date is strictly after the given day. */
    public boolean isDeletableOn(LocalDate today) {
        return date.isAfter(today);
    }

    /**
     * Identifiers occupied by this ticket in the shared ticket identifier space.
     * Ticket types that contain other tickets add the nested identifiers.
     */
    public Collection<String> identifiers() {
        return List.of(identifier);
    }

    /** @return whether this ticket was purchased for (or includes) the given user */
    public abstract boolean involves(User user);

    /** Users that enter an attraction together when this ticket is used. */
    public abstract List<User> admittedUsers();
}
