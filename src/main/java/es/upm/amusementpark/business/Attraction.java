package es.upm.amusementpark.business;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Park attraction accepting tickets with access capability {@code T}. It references the users currently inside
 * it (by e-mail); users never reference attractions.
 *
 * @param <T> access capability required to enter
 */
public abstract class Attraction<T extends GeneralAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final Set<String> occupants = new LinkedHashSet<>();

    protected Attraction(String id, String name, int capacity, Class<T> accessType) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.capacity = capacity;
        this.accessType = Objects.requireNonNull(accessType, "accessType");
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

    public abstract AttractionType getType();

    /** @return the access capability a ticket needs to enter */
    public Class<T> getAccessType() {
        return accessType;
    }

    /** @return whether the ticket has the capability this attraction is bound to */
    public boolean accepts(GeneralAccess ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Checks the type-specific admission conditions for a ticket and returns it typed by this attraction's
     * capability.
     *
     * @throws ParkException if the ticket kind is not accepted or the conditions are not met
     */
    public T admissionFor(GeneralAccess ticket, PassLookup passes) {
        if (!accepts(ticket)) {
            throw new ParkException("Attraction " + id + " (" + getType() + ") does not accept "
                    + (ticket instanceof GroupAccess ? "group" : "individual") + " tickets");
        }
        T typed = accessType.cast(ticket);
        checkAdmission(typed, passes);
        return typed;
    }

    /** Additional, type-specific admission conditions; none by default. */
    protected void checkAdmission(T ticket, PassLookup passes) {
        // no extra conditions
    }

    /** @return unmodifiable view of the e-mails of the users currently inside, in order of entry */
    public Set<String> getOccupants() {
        return Collections.unmodifiableSet(occupants);
    }

    /** @return capacity minus the number of users currently inside */
    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(String email) {
        return occupants.contains(email);
    }

    /** @return {@code false} if the attraction is full or the user is already inside */
    boolean enter(String email) {
        if (isFull() || occupants.contains(email)) {
            return false;
        }
        return occupants.add(email);
    }

    /** @return {@code false} if the user was not inside */
    boolean leave(String email) {
        return occupants.remove(email);
    }
}
