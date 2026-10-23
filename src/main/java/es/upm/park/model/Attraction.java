package es.upm.park.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside it; users do not
 * know which attraction they are in.
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

    public int getOccupancy() {
        return occupants.size();
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * Admits all users or none.
     *
     * @return {@code false} if there are not enough free places
     */
    public boolean enter(List<User> users) {
        if (users.size() > getAvailablePlaces()) {
            return false;
        }
        occupants.addAll(users);
        return true;
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
