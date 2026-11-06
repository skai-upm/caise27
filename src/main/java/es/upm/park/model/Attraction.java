package es.upm.park.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A park attraction with a maximum capacity and the users currently inside it
 * (referenced by email). Users do not refer to the attraction they are in.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final Set<String> occupantEmails = new LinkedHashSet<>();

    public Attraction(String id, String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
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

    /** Read-only view of the current occupants, in order of entry. */
    public Set<String> getOccupantEmails() {
        return Collections.unmodifiableSet(occupantEmails);
    }

    public boolean isInside(String email) {
        return occupantEmails.contains(email);
    }

    /** @return true if the user was added, false if already inside */
    public boolean enter(String email) {
        return occupantEmails.add(email);
    }

    /** @return true if the user was inside and left */
    public boolean leave(String email) {
        return occupantEmails.remove(email);
    }
}
