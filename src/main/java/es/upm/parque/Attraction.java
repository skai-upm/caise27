package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside it.
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

    public int available() {
        return capacity - present.size();
    }

    public boolean isEmpty() {
        return present.isEmpty();
    }

    public boolean contains(User user) {
        return present.contains(user);
    }

    public boolean canAdmit(List<User> users) {
        return users.size() <= available();
    }

    public void admit(List<User> users) {
        present.addAll(users);
    }

    /**
     * Removes the given users that are present and returns how many left.
     */
    public int release(List<User> users) {
        int before = present.size();
        present.removeAll(users);
        return before - present.size();
    }
}
