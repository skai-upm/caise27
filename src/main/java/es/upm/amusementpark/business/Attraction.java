package es.upm.amusementpark.business;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Park attraction. It references the users currently inside it (by e-mail); users never reference attractions.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final Set<String> occupants = new LinkedHashSet<>();

    public Attraction(String id, String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
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

    /** @return unmodifiable view of the e-mails of the users currently inside, in order of entry */
    public Set<String> getOccupants() {
        return Collections.unmodifiableSet(occupants);
    }

    /** @return capacity minus the number of users currently inside */
    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(String email) {
        return occupants.contains(email);
    }

    /** @return {@code false} if the attraction is full or the user is already inside */
    boolean enter(String email) {
        if (isFull() || occupants.contains(email)) {
            return false;
        }
        return occupants.add(email);
    }

    /** @return {@code false} if the user was not inside */
    boolean leave(String email) {
        return occupants.remove(email);
    }
}
