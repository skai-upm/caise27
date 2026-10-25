package com.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently on it. The type parameter
 * is the kind of ticket the attraction admits.
 *
 * @param <T> admitted ticket type
 */
public abstract class Attraction<T extends Ticket> {
    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
    }

    public abstract AttractionType getType();

    /**
     * Whether entry may use SpeedPasses.
     */
    public boolean allowsSpeed() {
        return true;
    }

    /**
     * Whether each admitted ticket must present an unused Special Pass.
     */
    public boolean requiresSpecialPass() {
        return false;
    }

    /**
     * Returns the ticket typed as this attraction's ticket kind, or null if it is not admitted here.
     */
    public T admit(Ticket ticket) {
        return admitted.isInstance(ticket) ? admitted.cast(ticket) : null;
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
     * Checks that all of the ticket's users can enter.
     *
     * @return null if they can, otherwise the reason
     */
    public String checkEntry(T ticket) {
        List<User> users = ticket.getUsers();
        for (User user : users) {
            if (present.contains(user)) {
                return "user " + user.getDni() + " is already in the attraction";
            }
        }
        if (users.size() > getAvailable()) {
            return "not enough capacity (" + getAvailable() + " available, " + users.size() + " needed)";
        }
        return null;
    }

    /**
     * Admits all users of the ticket, or none of them.
     *
     * @return null on success, otherwise the reason for rejection
     */
    public String enter(T ticket) {
        String error = checkEntry(ticket);
        if (error == null) {
            present.addAll(ticket.getUsers());
        }
        return error;
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
