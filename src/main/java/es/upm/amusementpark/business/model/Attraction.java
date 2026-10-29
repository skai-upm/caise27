package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.DuplicateElementException;
import es.upm.amusementpark.business.exception.InvalidValueException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A park attraction that accepts tickets with access capability {@code T}. It
 * holds the users currently inside it; users do not know the attraction they
 * are in.
 *
 * @param <T> the access capability of the tickets this attraction accepts
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String identifier;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String identifier, String name, int capacity, Class<T> accessType) {
        if (capacity <= 0) {
            throw new InvalidValueException("capacity must be greater than zero");
        }
        this.identifier = Objects.requireNonNull(identifier);
        this.name = Objects.requireNonNull(name);
        this.capacity = capacity;
        this.accessType = Objects.requireNonNull(accessType);
    }

    public abstract AttractionType getType();

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Class<T> getAccessType() {
        return accessType;
    }

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /** @return {@code false} if the attraction is full or the user is already inside */
    public boolean addOccupant(User user) {
        if (isFull() || isInside(user)) {
            return false;
        }
        return occupants.add(user);
    }

    /** @return {@code false} if the user was not inside */
    public boolean removeOccupant(User user) {
        return occupants.remove(user);
    }

    /** Whether this attraction accepts the ticket's kind of access. */
    public boolean accepts(AccessTicket ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * The ticket seen through this attraction's access capability.
     *
     * @throws OperationNotAllowedException if this attraction type does not accept that ticket
     */
    public T accessFor(AccessTicket ticket) {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException("attraction " + identifier + " of type " + getType()
                    + " does not accept ticket " + ticket.getIdentifier());
        }
        return accessType.cast(ticket);
    }

    /**
     * Entry with a ticket valid today, optionally using SpeedPasses. Every
     * condition is checked before any change: on success all admitted users
     * enter and the selected passes are consumed; otherwise nothing changes.
     */
    public Entry enter(T ticket, LocalDate today, PassLookup passes, boolean speed) {
        if (!ticket.getDate().equals(today)) {
            throw new OperationNotAllowedException("ticket " + ticket.getIdentifier() + " is valid on "
                    + ticket.getDate() + ", not on the current date " + today);
        }
        List<Pass> toConsume = passesToConsume(ticket, passes, speed);
        List<User> entrants = ticket.admittedUsers();
        for (User user : entrants) {
            if (isInside(user)) {
                throw new DuplicateElementException("user " + user.getEmail()
                        + " is already inside attraction " + identifier);
            }
        }
        if (occupants.size() + entrants.size() > capacity) {
            throw new OperationNotAllowedException("attraction " + identifier + " does not have enough capacity");
        }
        occupants.addAll(entrants);
        for (Pass pass : toConsume) {
            pass.consume();
        }
        return new Entry(entrants, toConsume);
    }

    /**
     * Passes this entry will consume. By default, with {@code speed} each
     * individual ticket admitted must have an unconsumed SpeedPass, one of
     * which is selected; without it no pass is used.
     */
    protected List<Pass> passesToConsume(T ticket, PassLookup passes, boolean speed) {
        List<Pass> selected = new ArrayList<>();
        if (!speed) {
            return selected;
        }
        for (IndividualTicket individual : ticket.individualTickets()) {
            Pass pass = passes.unconsumedSpeedPass(individual).orElseThrow(() -> new OperationNotAllowedException(
                    "ticket " + individual.getIdentifier() + " has no unconsumed speed pass"));
            selected.add(pass);
        }
        return selected;
    }
}
