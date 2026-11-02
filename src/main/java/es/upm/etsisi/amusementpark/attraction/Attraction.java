package es.upm.etsisi.amusementpark.attraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.user.User;

/**
 * A park attraction and the users currently inside it. Users hold no reference
 * back to the attraction. Occupancy lives only in memory.
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.stream().anyMatch(o -> o == user);
    }

    /**
     * Admits all users together; callers check capacity first.
     */
    public void admit(List<User> users) {
        if (users.size() > getAvailablePlaces()) {
            throw new IllegalStateException("not enough places in " + identifier);
        }
        occupants.addAll(users);
    }

    public boolean release(User user) {
        return occupants.removeIf(o -> o == user);
    }
}
