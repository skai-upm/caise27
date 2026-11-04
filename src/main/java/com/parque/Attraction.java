package com.parque;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently present. Visitors enter and leave with
 * any {@link Ticket}; the attraction only relies on {@link Ticket#getVisitors()}, so individual and group
 * tickets are handled by the same logic. Occupancy is not persisted.
 */
public class Attraction {
    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> present = new ArrayList<>();

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

    /** Users currently present (read-only view). */
    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int available() {
        return capacity - present.size();
    }

    public boolean isPresent(User user) {
        for (User u : present) {
            if (u == user) {
                return true;
            }
        }
        return false;
    }

    /**
     * Admits all visitors of the ticket or none: the ticket must be for {@code today}, none of its visitors may
     * already be inside, and they must all fit in the available places.
     */
    public void enter(Ticket ticket, LocalDate today) {
        if (!ticket.getDate().equals(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " is valid for " + ticket.getDate()
                    + ", not for the system date " + today);
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.isEmpty()) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " has no registered visitors");
        }
        for (User v : visitors) {
            if (isPresent(v)) {
                throw new IllegalArgumentException("User " + v.getEmail() + " is already in attraction " + id);
            }
        }
        if (visitors.size() > available()) {
            throw new IllegalArgumentException("Not enough places in attraction " + id + ": " + visitors.size()
                    + " needed, " + available() + " available");
        }
        present.addAll(visitors);
    }

    /** Removes the ticket's visitors that are present; returns how many left. Rejects if none was present. */
    public int exit(Ticket ticket) {
        int removed = 0;
        for (User v : ticket.getVisitors()) {
            if (present.removeIf(u -> u == v)) {
                removed++;
            }
        }
        if (removed == 0) {
            throw new IllegalArgumentException("No visitor of ticket " + ticket.getId() + " is in attraction " + id);
        }
        return removed;
    }
}
