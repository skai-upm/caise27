package com.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently on it.
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

    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    /**
     * Admits all users of the ticket, or none of them.
     *
     * @return null on success, otherwise the reason for rejection
     */
    public String enter(Ticket ticket) {
        List<User> users = ticket.getUsers();
        for (User user : users) {
            if (present.contains(user)) {
                return "user " + user.getDni() + " is already in the attraction";
            }
        }
        if (users.size() > getAvailable()) {
            return "not enough capacity (" + getAvailable() + " available, " + users.size() + " needed)";
        }
        present.addAll(users);
        return null;
    }

    /**
     * Removes the ticket's users that are present.
     *
     * @return number of users that left
     */
    public int exit(Ticket ticket) {
        int left = 0;
        for (User user : ticket.getUsers()) {
            if (present.remove(user)) {
                left++;
            }
        }
        return left;
    }
}
