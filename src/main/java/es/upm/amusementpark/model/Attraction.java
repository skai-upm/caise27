package es.upm.amusementpark.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside it; users do not know the attraction.
 */
public class Attraction {

    private final String id;
    private String name;
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

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int occupancy() {
        return occupants.size();
    }

    public int freePlaces() {
        return capacity - occupants.size();
    }

    public boolean hasRoomFor(int people) {
        return people <= freePlaces();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /** Adds a user inside; the caller checks capacity beforehand. */
    public void enter(User user) {
        occupants.add(user);
    }

    /** Removes a user; returns false if the user was not inside. */
    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
