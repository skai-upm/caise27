package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.RuleViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction that only handles tickets of the access capability {@code T}.
 * It holds the users currently inside it (users do not know the attraction).
 *
 * @param <T> ticket access capability accepted by this attraction
 */
public abstract class Attraction<T extends TicketAccess> {

    private final String identifier;
    private String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String identifier, String name, int capacity, Class<T> accessType) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.identifier = identifier;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
    }

    public abstract AttractionType getType();

    /** Whether tickets with this access capability can be used here. */
    public boolean accepts(TicketAccess ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Entry gate: only tickets of capability {@code T} reach the typed entry operations.
     *
     * @return the users who entered
     */
    public final List<User> enter(TicketAccess ticket) {
        if (!accepts(ticket)) {
            throw new RuleViolationException(getType() + " attraction " + identifier + " does not accept ticket '"
                    + ticket.getIdentifier() + "'");
        }
        T access = accessType.cast(ticket);
        checkEntry(access);
        List<User> users = admittedUsers(access);
        if (users.stream().anyMatch(this::contains)) {
            throw new RuleViolationException("a user of ticket '" + ticket.getIdentifier() + "' is already inside " + identifier);
        }
        if (!admitAll(users)) {
            throw new RuleViolationException("not enough capacity in " + identifier + " (" + availableCapacity()
                    + " available, " + users.size() + " needed)");
        }
        return users;
    }

    /** Type-specific entry conditions, checked before anybody enters. */
    protected void checkEntry(T access) {
        // no additional conditions by default
    }

    /** Users admitted by an accepted ticket. */
    protected List<User> admittedUsers(T access) {
        return access.admittedUsers();
    }

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

    public int availableCapacity() {
        return capacity - occupants.size();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /** Whether all the given users could enter now (enough room and none already inside). */
    public boolean canAdmit(Collection<User> users) {
        return users.size() <= availableCapacity() && users.stream().noneMatch(this::contains)
                && users.stream().distinct().count() == users.size();
    }

    /** Admits all the given users or none of them. */
    public boolean admitAll(Collection<User> users) {
        if (!canAdmit(users)) {
            return false;
        }
        occupants.addAll(users);
        return true;
    }

    public boolean remove(User user) {
        return occupants.remove(user);
    }
}
