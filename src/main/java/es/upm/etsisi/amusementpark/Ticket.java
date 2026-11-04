package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * An individual ticket for one date. It refers to the existing {@link User}
 * who owns it; if that user is deleted the ticket stays as a historical record
 * that keeps the owner's DNI.
 */
public class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;
    private final String ownerDni;
    private User user;

    public Ticket(String id, LocalDate date, User user, BigDecimal price) {
        this(id, date, user.getDni(), user, price);
    }

    /** Ticket whose owner may no longer exist ({@code user} may be null). */
    public Ticket(String id, LocalDate date, String ownerDni, User user, BigDecimal price) {
        this.id = id;
        this.date = date;
        this.ownerDni = ownerDni;
        this.user = user;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

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

    public boolean belongsTo(User candidate) {
        return user != null && user == candidate;
    }

    /** Drops the reference to a deleted owner, keeping the DNI as history. */
    public void detachUser() {
        user = null;
    }
}
