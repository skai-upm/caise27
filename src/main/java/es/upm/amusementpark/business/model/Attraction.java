package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction. It holds the users currently inside it (users do not know the attraction).
 */
public class Attraction {

    private final String identifier;
    private String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    public Attraction(String identifier, String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
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

    public int availableCapacity() {
        return capacity - occupants.size();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /** Whether all the given users could enter now (enough room and none already inside). */
    public boolean canAdmit(Collection<User> users) {
        return users.size() <= availableCapacity() && users.stream().noneMatch(this::contains)
                && users.stream().distinct().count() == users.size();
    }

    /** Admits all the given users or none of them. */
    public boolean admitAll(Collection<User> users) {
        if (!canAdmit(users)) {
            return false;
        }
        occupants.addAll(users);
        return true;
    }

    public boolean remove(User user) {
        return occupants.remove(user);
    }
}
