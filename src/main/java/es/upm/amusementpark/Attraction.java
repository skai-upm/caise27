package es.upm.amusementpark;

import java.util.ArrayList;
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
        return List.copyOf(occupants);
    }

    public int getAvailable() {
        return capacity - occupants.size();
    }

    /**
     * Admits all the given users or none of them.
     */
    void enter(List<User> users) {
        if (users.size() > getAvailable()) {
            throw new IllegalArgumentException("not enough capacity in attraction " + id
                    + " (" + getAvailable() + " available, " + users.size() + " needed)");
        }
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new IllegalArgumentException("user already inside attraction " + id + ": " + user.getEmail());
            }
        }
        occupants.addAll(users);
    }

    void exit(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                occupants.remove(user);
                return;
            }
        }
        throw new IllegalArgumentException("user not inside attraction " + id + ": " + email);
    }
}
