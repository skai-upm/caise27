package es.upm.etsisi.amusementpark;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction. It knows the users inside it; users do not know the attraction.
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
     * Admits all users together or none of them.
     */
    public void enter(List<User> users) throws OperationException {
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new OperationException("user already inside the attraction: " + user.getEmail());
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new OperationException("not enough capacity: " + users.size() + " needed, "
                    + getAvailablePlaces() + " available");
        }
        occupants.addAll(users);
    }

    public User findOccupant(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public void exit(User user) {
        occupants.remove(user);
    }
}
