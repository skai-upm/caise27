package es.upm.etsisi.amusementpark.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction that knows which users are currently inside it.
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
     * Lets all users in together or none of them.
     */
    public void enter(List<User> users) {
        if (users.size() > getAvailablePlaces()) {
            throw new IllegalArgumentException("Not enough capacity: " + users.size()
                    + " user(s) need to enter, " + getAvailablePlaces() + " place(s) available");
        }
        for (User user : users) {
            if (isInside(user)) {
                throw new IllegalArgumentException("User already inside: " + user.getEmail());
            }
        }
        occupants.addAll(users);
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
