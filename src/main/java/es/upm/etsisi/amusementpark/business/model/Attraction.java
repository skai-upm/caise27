package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently present. Users enter with any
 * {@link ParkTicket}: the ticket decides which users it admits, so entry is handled uniformly.
 * Holds state only; the users present are never persisted.
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

    /** Users currently present, in order of entry. */
    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    public boolean isPresent(User user) {
        return present.stream().anyMatch(u -> u == user);
    }

    /**
     * Admits every user of the ticket, or none: the ticket must be dated {@code today}, no user may already be
     * present and all of them must fit in the remaining capacity.
     */
    public void enter(ParkTicket ticket, LocalDate today) {
        if (!ticket.getDate().equals(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " is not valid on " + today
                    + " (it is dated " + ticket.getDate() + ")");
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.isEmpty()) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " has no registered users");
        }
        for (User u : visitors) {
            if (isPresent(u)) {
                throw new IllegalArgumentException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (visitors.size() > getAvailable()) {
            throw new IllegalArgumentException("Not enough capacity in attraction " + id + ": " + visitors.size()
                    + " users, " + getAvailable() + " places available");
        }
        present.addAll(visitors);
    }

    /** Removes the given users that are present; returns how many left. */
    public int exit(Collection<User> users) {
        int before = present.size();
        present.removeIf(p -> users.stream().anyMatch(u -> u == p));
        return before - present.size();
    }
}
