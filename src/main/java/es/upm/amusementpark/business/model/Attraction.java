package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.AttractionOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside it; users do not
 * know which attraction they are in. The type parameter is the ticket access
 * capability the attraction accepts.
 *
 * @param <T> the kind of ticket this attraction can handle
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String identifier;
    private String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String identifier, String name, int capacity, Class<T> accessType) {
        this.identifier = identifier;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
    }

    public abstract AttractionType getType();

    public String getIdentifier() {
        return identifier;
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * Checks that the ticket is of the kind this attraction accepts and meets
     * its own conditions.
     *
     * @return the ticket as the accepted access type
     */
    public T accept(Ticket ticket, PassLookup passes) throws AttractionOperationException {
        if (!accessType.isInstance(ticket)) {
            throw new AttractionOperationException("Attraction " + identifier + " of type " + getType()
                    + " does not accept ticket " + ticket.getIdentifier() + ".");
        }
        T access = accessType.cast(ticket);
        checkAdmission(access, passes);
        return access;
    }

    /** Whether entry with SpeedPasses is allowed; true unless the attraction manages its own queue. */
    public boolean acceptsSpeedPasses() {
        return true;
    }

    /**
     * Passes consumed automatically when the ticket's users have entered;
     * none by default.
     */
    public List<Pass> consumeOnEntry(T ticket, PassLookup passes) {
        return List.of();
    }

    /** Conditions specific to the attraction type; none by default. */
    protected void checkAdmission(T ticket, PassLookup passes) throws AttractionOperationException {
    }

    /**
     * Lets all users of the ticket in together, or none if any is already
     * inside or there is not enough room.
     */
    public List<User> admit(T ticket) throws AttractionOperationException {
        List<User> holders = ticket.getHolders();
        for (User user : holders) {
            if (isInside(user)) {
                throw new AttractionOperationException("User " + user.getEmail() + " is already inside attraction "
                        + identifier + ".");
            }
        }
        if (holders.size() > getAvailablePlaces()) {
            throw new AttractionOperationException("Attraction " + identifier + " does not have enough capacity ("
                    + getAvailablePlaces() + " places available).");
        }
        enter(holders);
        return holders;
    }

    /** Lets all the given users in together. */
    public void enter(List<User> users) {
        occupants.addAll(users);
    }

    /** @return {@code false} if the user was not inside */
    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
