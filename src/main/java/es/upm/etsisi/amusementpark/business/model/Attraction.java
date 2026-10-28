package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A park attraction. It references the users currently inside it; users do not
 * reference attractions.
 */
public class Attraction {

    private final String identifier;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    public Attraction(String identifier, String name, int capacity) {
        this.identifier = identifier;
        this.name = name;
        this.capacity = capacity;
    }

    public String getIdentifier() {
        return identifier;
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public Optional<User> findOccupant(String email) {
        return occupants.stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * @throws OperationNotAllowedException if the attraction is full or the user is already inside
     */
    public void addOccupant(User user) {
        if (isInside(user)) {
            throw new OperationNotAllowedException("User " + user.getEmail() + " is already inside attraction " + identifier);
        }
        if (isFull()) {
            throw new OperationNotAllowedException("Attraction " + identifier + " is full");
        }
        occupants.add(user);
    }

    /**
     * Admits all {@code users} together or none of them.
     *
     * @throws OperationNotAllowedException if any user is already inside or capacity is insufficient for all
     */
    public void admit(List<User> users) {
        for (User user : users) {
            if (isInside(user)) {
                throw new OperationNotAllowedException("User " + user.getEmail() + " is already inside attraction " + identifier);
            }
        }
        if (occupants.size() + users.size() > capacity) {
            throw new OperationNotAllowedException("Attraction " + identifier + " does not have capacity for "
                    + users.size() + " user(s)");
        }
        occupants.addAll(users);
    }

    /**
     * @throws NotFoundException if the user is not inside the attraction
     */
    public void removeOccupant(User user) {
        if (!occupants.remove(user)) {
            throw new NotFoundException("User " + user.getEmail() + " is not inside attraction " + identifier);
        }
    }
}
