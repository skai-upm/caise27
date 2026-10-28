package es.upm.amusementpark.model;

import java.util.ArrayList;
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

    /**
     * Maximum number of people inside at the same time.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Users currently inside, in order of entry.
     */
    public List<User> getOccupants() {
        return new ArrayList<>(occupants);
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    public void addOccupant(User user) {
        occupants.add(user);
    }

    public boolean removeOccupant(User user) {
        return occupants.remove(user);
    }
}
