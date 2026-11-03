package es.upm.etsisi.park;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside it; users do not know
 * the attraction. Occupancy is runtime state only.
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * Admits all the given users or none of them.
     */
    void enter(List<User> users) throws ValidationException {
        if (users.size() > getAvailablePlaces()) {
            throw new ValidationException("not enough places in attraction " + id + ": "
                    + users.size() + " needed, " + getAvailablePlaces() + " available");
        }
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new ValidationException("user " + user.getEmail() + " is already inside attraction " + id);
            }
        }
        occupants.addAll(users);
    }

    void exit(User user) {
        occupants.remove(user);
    }

    void restore(List<User> previous) {
        occupants.clear();
        occupants.addAll(previous);
    }

    User findOccupant(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}
