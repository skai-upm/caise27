package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.InvalidValueException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A park attraction. It holds the users currently inside it; users do not
 * know the attraction they are in.
 */
public class Attraction {

    private final String identifier;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    public Attraction(String identifier, String name, int capacity) {
        if (capacity <= 0) {
            throw new InvalidValueException("capacity must be greater than zero");
        }
        this.identifier = Objects.requireNonNull(identifier);
        this.name = Objects.requireNonNull(name);
        this.capacity = capacity;
    }

    public String getIdentifier() {
        return identifier;
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
}
