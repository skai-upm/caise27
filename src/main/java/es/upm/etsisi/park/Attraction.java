package es.upm.etsisi.park;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An attraction with a maximum capacity and the users currently inside it.
 * Occupants are runtime state only and are never persisted.
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

    /** Admits all users together or none of them. */
    void enter(List<User> users) {
        if (users.size() > getAvailable()) {
            throw new ParkException("Not enough capacity: " + users.size() + " needed, " + getAvailable() + " available");
        }
        for (User u : users) {
            if (occupants.contains(u)) {
                throw new ParkException("User already inside: " + u.getEmail());
            }
        }
        occupants.addAll(users);
    }

    void exit(String email) {
        for (int i = 0; i < occupants.size(); i++) {
            if (occupants.get(i).getEmail().equals(email)) {
                occupants.remove(i);
                return;
            }
        }
        throw new ParkException("User not inside attraction: " + email);
    }
}
