package es.upm.parque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside it.
 * The attraction knows its occupants; users do not know the attraction they are in.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    public Attraction(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
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

    /** Users currently inside, in order of entry; read-only view. */
    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public boolean isInside(User user) {
        return occupants.stream().anyMatch(u -> u.getDni().equals(user.getDni()));
    }

    /** Adds the user unless already inside; returns whether it was added. */
    public boolean addOccupant(User user) {
        if (isInside(user)) {
            return false;
        }
        occupants.add(user);
        return true;
    }

    /** Removes the user if inside; returns whether it was removed. */
    public boolean removeOccupant(User user) {
        return occupants.removeIf(u -> u.getDni().equals(user.getDni()));
    }
}
