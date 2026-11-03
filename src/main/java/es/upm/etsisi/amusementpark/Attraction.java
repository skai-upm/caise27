package es.upm.etsisi.amusementpark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It references the users currently inside it; users do not
 * reference attractions. Occupants are never persisted.
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
        return occupants.contains(user);
    }

    public void admit(Collection<User> users) {
        occupants.addAll(users);
    }

    public void release(User user) {
        occupants.remove(user);
    }
}
