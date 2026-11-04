package es.upm.park;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A park attraction with a maximum capacity and the users currently present.
 * Entry and exit work on any {@link Ticket} through {@link Ticket#getHolders()}.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final Set<User> present = new LinkedHashSet<>();

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

    public List<User> getPresentUsers() {
        return Collections.unmodifiableList(new ArrayList<>(present));
    }

    public int getPresentCount() {
        return present.size();
    }

    public int getAvailablePlaces() {
        return capacity - present.size();
    }

    /**
     * Admits every holder of {@code ticket}, or none: the ticket must be dated
     * {@code today}, no holder may already be present and capacity must suffice.
     */
    public void enter(Ticket ticket, LocalDate today) {
        requireValid(ticket.getDate().equals(today),
                "ticket " + ticket.getId() + " is valid for " + ticket.getDate() + ", not for " + today);
        List<User> holders = ticket.getHolders();
        requireValid(!holders.isEmpty() && !holders.contains(null),
                "ticket " + ticket.getId() + " has no current holders");
        for (User u : holders) {
            requireValid(!present.contains(u), "user " + u.getEmail() + " is already in attraction " + id);
        }
        requireValid(holders.size() <= getAvailablePlaces(), "not enough capacity in attraction " + id
                + ": " + holders.size() + " requested, " + getAvailablePlaces() + " available");
        present.addAll(holders);
    }

    /** Removes the holders of {@code ticket} that are present; returns how many left. */
    public int exit(Ticket ticket) {
        int removed = 0;
        for (User u : ticket.getHolders()) {
            if (u != null && present.remove(u)) {
                removed++;
            }
        }
        requireValid(removed > 0, "no holder of ticket " + ticket.getId() + " is in attraction " + id);
        return removed;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
