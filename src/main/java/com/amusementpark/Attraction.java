package com.amusementpark;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> visitors = new ArrayList<>();

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

    public List<User> getVisitors() {
        return Collections.unmodifiableList(visitors);
    }

    public int getAvailable() {
        return capacity - visitors.size();
    }

    /**
     * Admits every holder of the ticket, or nobody.
     *
     * @return {@code null} on success, otherwise the reason for rejection
     */
    public String enter(Ticket ticket, LocalDate today) {
        if (!ticket.isValidOn(today)) {
            return "Ticket " + ticket.getId() + " is valid on " + ticket.getDate() + ", not on " + today + ".";
        }
        List<User> holders = ticket.getHolders();
        for (User user : holders) {
            if (visitors.contains(user)) {
                return user.getEmail() + " is already in " + id + ".";
            }
        }
        if (holders.size() > getAvailable()) {
            return "Not enough places in " + id + ": " + holders.size() + " needed, " + getAvailable() + " available.";
        }
        visitors.addAll(holders);
        return null;
    }

    /** Removes the given users that are present; returns how many left. */
    public int exit(Collection<User> users) {
        int before = visitors.size();
        visitors.removeAll(users);
        return before - visitors.size();
    }
}
