package es.upm.etsisi.amusementpark.attraction;

import es.upm.etsisi.amusementpark.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside; users do not know attractions.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    public Attraction(String id, String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getFreePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /** Checks whether all the given users could enter together. */
    public boolean canAdmit(Collection<User> users) {
        return users.size() <= getFreePlaces() && users.stream().noneMatch(this::isInside);
    }

    /** Admits all the given users together, or none of them. */
    public void admit(Collection<User> users) {
        if (!canAdmit(users)) {
            throw new IllegalStateException("cannot admit users to attraction " + id);
        }
        occupants.addAll(users);
    }

    /** Removes a user, returning whether they were inside. */
    public boolean release(User user) {
        return occupants.remove(user);
    }
}
