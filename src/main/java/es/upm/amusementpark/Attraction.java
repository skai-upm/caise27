package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside it; users do not know the attraction.
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    /**
     * Lets all the given users in together, or none of them.
     */
    public void enter(List<User> users) {
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new IllegalArgumentException("user " + user.getEmail() + " is already inside attraction " + id);
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new IllegalArgumentException("not enough capacity in attraction " + id + ": " + users.size()
                    + " user(s) need to enter and " + getAvailablePlaces() + " place(s) are available");
        }
        occupants.addAll(users);
    }

    public void exit(User user) {
        if (!occupants.remove(user)) {
            throw new IllegalArgumentException("user " + user.getEmail() + " is not inside attraction " + id);
        }
    }
}
