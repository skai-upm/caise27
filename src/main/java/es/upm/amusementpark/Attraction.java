package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A park attraction with a maximum capacity and the users currently on it. */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> visitors = new ArrayList<>();

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

    public List<User> getVisitors() {
        return Collections.unmodifiableList(visitors);
    }

    public int getAvailablePlaces() {
        return capacity - visitors.size();
    }

    public boolean isPresent(User user) {
        return visitors.contains(user);
    }

    public void enter(List<User> users) {
        visitors.addAll(users);
    }

    public void leave(User user) {
        visitors.remove(user);
    }
}
