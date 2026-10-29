package es.upm.amusementpark.model;

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

    public int getFreePlaces() {
        return capacity - occupants.size();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /**
     * Admits all the given users, or none if there is not room for all of them.
     *
     * @return whether the users were admitted
     */
    public boolean enter(List<User> users) {
        if (users.size() > getFreePlaces()) {
            return false;
        }
        occupants.addAll(users);
        return true;
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
