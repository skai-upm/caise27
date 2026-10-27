package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It knows the users currently inside it; users do not
 * know which attraction they are in.
 */
public class Attraction {

    private final String identifier;
    private String name;
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

    public void setName(String name) {
        this.name = name;
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

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /** Lets all the given users in together. */
    public void enter(List<User> users) {
        occupants.addAll(users);
    }

    /** @return {@code false} if the user was not inside */
    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
