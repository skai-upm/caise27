package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

/**
 * A park attraction admitting tickets with the access capability {@code T}. It knows the users
 * currently inside it; users do not know the attraction.
 *
 * @param <T> access capability accepted by this attraction
 */
public abstract class Attraction<T extends AttractionAccess> {

    private final String id;
    private String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accessType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
    }

    public abstract AttractionType getType();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int occupancy() {
        return occupants.size();
    }

    public int freePlaces() {
        return capacity - occupants.size();
    }

    public boolean hasRoomFor(int people) {
        return people <= freePlaces();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /** The access as this attraction's access type, if the attraction accepts it. */
    public T accessFor(AttractionAccess access) throws OperationNotAllowedException {
        if (!accessType.isInstance(access)) {
            throw new OperationNotAllowedException("Attraction " + id + " (" + getType()
                    + ") does not accept this ticket type");
        }
        return accessType.cast(access);
    }

    /**
     * Lets every holder of the access in, all together or none, consuming the passes the entry
     * requires. Every condition is checked before any state changes: validity today, required
     * passes, nobody already inside and room for all.
     */
    public EntryResult admit(T access, LocalDate today, boolean speed, PassLookup passes)
            throws OperationNotAllowedException {
        if (!access.isValidOn(today)) {
            throw new OperationNotAllowedException("Ticket is not valid for the current date");
        }
        List<Pass> toConsume = passesToConsume(access, speed, passes);
        List<User> holders = access.holders();
        for (User user : holders) {
            if (isInside(user)) {
                throw new OperationNotAllowedException(user.getEmail() + " is already inside " + id);
            }
        }
        if (!hasRoomFor(holders.size())) {
            throw new OperationNotAllowedException("Not enough capacity in " + id);
        }
        for (Pass pass : toConsume) {
            pass.consume();
        }
        occupants.addAll(holders);
        return new EntryResult(holders, toConsume);
    }

    /** Plain entry without passes. */
    public EntryResult admit(T access, LocalDate today) throws OperationNotAllowedException {
        return admit(access, today, false, ticket -> List.of());
    }

    /**
     * Passes this attraction consumes for the entry. By default none, or with {@code speed} one
     * unconsumed SpeedPass for every individual ticket of the access.
     */
    protected List<Pass> passesToConsume(T access, boolean speed, PassLookup passes)
            throws OperationNotAllowedException {
        List<Pass> result = new ArrayList<>();
        if (!speed) {
            return result;
        }
        for (IndividualTicket ticket : access.individualTickets()) {
            Pass speedPass = firstUnconsumed(passes.passesOf(ticket), SpeedPass.class);
            if (speedPass == null) {
                throw new OperationNotAllowedException("Ticket " + ticket.getId() + " has no available SpeedPass");
            }
            result.add(speedPass);
        }
        return result;
    }

    /** First unconsumed pass of the given type, or null. */
    protected static Pass firstUnconsumed(List<Pass> passes, Class<? extends Pass> type) {
        for (Pass pass : passes) {
            if (type.isInstance(pass) && !pass.isConsumed()) {
                return pass;
            }
        }
        return null;
    }

    /** Adds a user inside; the caller checks capacity beforehand. */
    public void enter(User user) {
        occupants.add(user);
    }

    /** Removes a user; returns false if the user was not inside. */
    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
