package es.upm.park;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An individual ticket for one date of use. It references the owning {@link User};
 * the owner's DNI is kept as well so the ticket remains a historical record after
 * the owner has been deleted.
 */
public class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;
    private final String ownerDni;
    private final User owner;

    public Ticket(String id, User owner, LocalDate date, BigDecimal price) {
        this(id, owner, owner.getDni(), date, price);
    }

    Ticket(String id, User owner, String ownerDni, LocalDate date, BigDecimal price) {
        this.id = id;
        this.owner = owner;
        this.ownerDni = ownerDni;
        this.date = date;
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

    /** DNI of the user who bought the ticket (also available for deleted owners). */
    public String getOwnerDni() {
        return ownerDni;
    }

    /** The owning user, or {@code null} for a historical ticket whose owner was deleted. */
    public User getOwner() {
        return owner;
    }

    public boolean isOwnedBy(User user) {
        return owner != null && owner == user;
    }
}
