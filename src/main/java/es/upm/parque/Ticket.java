package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An individual ticket for one date of use. It references the registered user it was sold to;
 * the owner's DNI is kept so the ticket remains identifiable as history after the user is deleted.
 */
public class Ticket {

    private final String id;
    private final LocalDate date;
    private final BigDecimal price;
    private final String ownerDni;
    private User owner;

    public Ticket(String id, LocalDate date, BigDecimal price, User owner) {
        this(id, date, price, owner.getDni(), owner);
    }

    Ticket(String id, LocalDate date, BigDecimal price, String ownerDni, User owner) {
        this.id = id;
        this.date = date;
        this.price = price;
        this.ownerDni = ownerDni;
        this.owner = owner;
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

    /** @return the registered owner, or null if that user no longer exists */
    public User getOwner() {
        return owner;
    }

    public boolean isOwnedBy(User user) {
        return owner == user;
    }

    /** Called when the owner is deleted; the ticket stays as a historical record. */
    public void detachOwner() {
        owner = null;
    }
}
