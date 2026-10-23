package es.upm.etsisi.park.model.attraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.park.user.User;

/**
 * A park attraction with a maximum capacity. It knows the users currently
 * inside it; users hold no reference back to the attraction.
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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getAvailable() {
        return capacity - occupants.size();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /** Lets all the users in together, or none of them if they do not fit. */
    public boolean enter(List<User> users) {
        if (users.size() > getAvailable()) {
            return false;
        }
        occupants.addAll(users);
        return true;
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }

    public User findOccupantByEmail(String email) {
        for (User user : occupants) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }
}
