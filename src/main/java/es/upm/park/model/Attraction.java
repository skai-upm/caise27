package es.upm.park.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * Only the attraction references its occupants; users know nothing of it.
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

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /** Lets a user in; returns false if already inside or full. */
    public boolean enter(User user) {
        if (contains(user) || isFull()) {
            return false;
        }
        return occupants.add(user);
    }

    public boolean leave(User user) {
        return occupants.remove(user);
    }
}
