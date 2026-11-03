package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * An individual ticket for a date of use, owned by a registered user and priced when it was bought.
 * The owner's DNI is kept as well, so a historical ticket remains identifiable after its user has been
 * deleted. Holds state only; it knows nothing about persistence.
 */
public class Ticket extends ParkTicket {

    private final BigDecimal price;
    private final User user;
    private final String ownerDni;

    /** Ticket of a registered user. */
    public Ticket(String id, User user, LocalDate date, BigDecimal price) {
        this(id, user, user.getDni(), date, price);
    }

    /** Historical ticket whose user is no longer registered. */
    public Ticket(String id, String ownerDni, LocalDate date, BigDecimal price) {
        this(id, null, ownerDni, date, price);
    }

    private Ticket(String id, User user, String ownerDni, LocalDate date, BigDecimal price) {
        super(id, date);
        this.user = user;
        this.ownerDni = ownerDni;
        this.price = price.setScale(2, RoundingMode.UNNECESSARY);
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    /** The owning user, empty for a historical ticket whose user no longer exists. */
    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> getVisitors() {
        return user == null ? List.of() : List.of(user);
    }

    public String getOwnerDni() {
        return ownerDni;
    }

    /** True if this ticket belongs to exactly this user object. */
    public boolean belongsTo(User candidate) {
        return user == candidate;
    }
}
