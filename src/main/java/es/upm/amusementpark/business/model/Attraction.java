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
     * Lets every holder of the access in, all together or none: the access must be valid today,
     * nobody may already be inside and there must be room for all.
     */
    public List<User> admit(T access, LocalDate today) throws OperationNotAllowedException {
        if (!access.isValidOn(today)) {
            throw new OperationNotAllowedException("Ticket is not valid for the current date");
        }
        List<User> holders = access.holders();
        for (User user : holders) {
            if (isInside(user)) {
                throw new OperationNotAllowedException(user.getEmail() + " is already inside " + id);
            }
        }
        if (!hasRoomFor(holders.size())) {
            throw new OperationNotAllowedException("Not enough capacity in " + id);
        }
        occupants.addAll(holders);
        return holders;
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
