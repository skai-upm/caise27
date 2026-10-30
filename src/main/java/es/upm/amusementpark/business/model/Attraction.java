package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> present = new ArrayList<>();

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

    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    /** Admits all users or none: fails if capacity is insufficient or any user is already inside. */
    public boolean enterAll(List<User> users) {
        if (users.isEmpty() || users.size() > getAvailable()) {
            return false;
        }
        for (User u : users) {
            if (present.contains(u)) {
                return false;
            }
        }
        present.addAll(users);
        return true;
    }

    /** Removes the given users that are inside and returns how many left. */
    public int exitAll(List<User> users) {
        int count = 0;
        for (User u : users) {
            if (present.remove(u)) {
                count++;
            }
        }
        return count;
    }
}
