package es.upm.etsisi.amusementpark.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It references the users currently inside it; users do not
 * reference attractions.
 */
public class Attraction {

    private final String identifier;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    public Attraction(String identifier, String name, int capacity) {
        this.identifier = identifier;
        this.name = name;
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

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * @throws IllegalStateException if the attraction is full or the user is already inside
     */
    public void addOccupant(User user) {
        if (isInside(user)) {
            throw new IllegalStateException("User " + user.getEmail() + " is already inside attraction " + identifier);
        }
        if (isFull()) {
            throw new IllegalStateException("Attraction " + identifier + " is full");
        }
        occupants.add(user);
    }

    /**
     * @throws IllegalStateException if the user is not inside the attraction
     */
    public void removeOccupant(User user) {
        if (!occupants.remove(user)) {
            throw new IllegalStateException("User " + user.getEmail() + " is not inside attraction " + identifier);
        }
    }
}
