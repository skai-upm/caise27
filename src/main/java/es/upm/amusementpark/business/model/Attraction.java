package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A park attraction with a maximum capacity. It knows the users currently
 * inside it; users do not know which attraction they are in.
 *
 * <p>The type parameter is the access capability the attraction accepts:
 * only tickets implementing it can be admitted, and the pass rules of each
 * attraction type are written against that capability.
 *
 * @param <T> the access capability of the tickets this attraction accepts
 */
public abstract class Attraction<T extends GeneralAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();
    private final Class<T> accessType;

    protected Attraction(String id, String name, int capacity, Class<T> accessType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
    }

    public abstract AttractionType getType();

    /** The access capability this attraction accepts. */
    public Class<T> getAccessType() {
        return accessType;
    }

    public boolean accepts(GeneralAccess ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Returns {@code ticket} with the capability this attraction works with.
     *
     * @throws OperationNotAllowedException if this attraction type does not accept that kind of ticket
     */
    public T admit(GeneralAccess ticket) throws OperationNotAllowedException {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException("Attraction " + id + " (" + getType() + ") does not accept "
                    + (ticket instanceof GroupAccess ? "group" : "individual") + " tickets");
        }
        return accessType.cast(ticket);
    }

    /**
     * Checks the entry option against this attraction's rules and returns the
     * passes the entry would consume, without changing anything.
     *
     * @param speed whether the SPEED option was requested
     * @throws OperationNotAllowedException if the option is not allowed or a required pass is missing
     */
    public abstract List<Pass> requiredPasses(T ticket, boolean speed, PassWallet wallet)
            throws OperationNotAllowedException;

    /** One unconsumed SpeedPass for each of {@code tickets}. */
    protected static List<Pass> oneSpeedPassEach(List<? extends IndividualAccess> tickets, PassWallet wallet)
            throws OperationNotAllowedException {
        List<Pass> passes = new ArrayList<>();
        for (IndividualAccess ticket : tickets) {
            SpeedPass pass = wallet.availableSpeedPass(ticket);
            if (pass == null) {
                throw new OperationNotAllowedException("Ticket " + ticket.getId()
                        + " has no unconsumed SpeedPass; nobody entered");
            }
            passes.add(pass);
        }
        return passes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getFreePlaces() {
        return capacity - occupants.size();
    }

    public boolean hasRoomFor(int people) {
        return people <= getFreePlaces();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /**
     * Lets all {@code users} in together.
     *
     * @throws IllegalStateException if they do not all fit
     */
    public void enter(Collection<User> users) {
        if (!hasRoomFor(users.size())) {
            throw new IllegalStateException("Not enough room in attraction " + id);
        }
        occupants.addAll(users);
    }

    /** Returns the occupant with that email (ignoring case), or {@code null}. */
    public User findOccupant(String email) {
        String wanted = email.toLowerCase(Locale.ROOT);
        for (User user : occupants) {
            if (user.getEmail().toLowerCase(Locale.ROOT).equals(wanted)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Removes {@code user} from the attraction.
     *
     * @return the position the user had, or {@code -1} if not inside
     */
    public int exit(User user) {
        int position = occupants.indexOf(user);
        if (position >= 0) {
            occupants.remove(position);
        }
        return position;
    }

    /** Puts {@code user} back at {@code position}, used to undo an exit. */
    public void restore(int position, User user) {
        if (!contains(user)) {
            occupants.add(Math.min(position, occupants.size()), user);
        }
    }
}
