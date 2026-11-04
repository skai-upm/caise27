package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * An individual ticket for one date. It refers to the existing {@link User}
 * who owns it; if that user is deleted the ticket stays as a historical record
 * that keeps the owner's DNI.
 */
public class IndividualTicket extends Ticket {

    private final BigDecimal price;
    private final String ownerDni;
    private User user;

    public IndividualTicket(String id, LocalDate date, User user, BigDecimal price) {
        this(id, date, user.getDni(), user, price);
    }

    /** Ticket whose owner may no longer exist ({@code user} may be null). */
    public IndividualTicket(String id, LocalDate date, String ownerDni, User user, BigDecimal price) {
        super(id, date);
        this.ownerDni = ownerDni;
        this.user = user;
        this.price = price;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    public String getOwnerDni() {
        return ownerDni;
    }

    /** The owning user, or empty if that user has been deleted. */
    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    /** Its owner, or nobody if the owner has been deleted. */
    @Override
    public List<User> getVisitors() {
        return user == null ? List.of() : List.of(user);
    }

    public boolean belongsTo(User candidate) {
        return user != null && user == candidate;
    }

    /** Drops the reference to a deleted owner, keeping the DNI as history. */
    public void detachUser() {
        user = null;
    }
}
