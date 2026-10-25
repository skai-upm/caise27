package es.upm.etsisi.park.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A park attraction. It knows which users are inside it; users do not know the attraction.
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
     * Admits every user or none of them.
     *
     * @return {@code false} if capacity is insufficient or any user is already inside
     */
    public boolean admitAll(List<User> users) {
        if (users.size() > getAvailablePlaces()) {
            return false;
        }
        for (User user : users) {
            if (occupants.contains(user)) {
                return false;
            }
        }
        occupants.addAll(users);
        return true;
    }

    public Optional<User> findOccupant(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean release(User user) {
        return occupants.remove(user);
    }
}
