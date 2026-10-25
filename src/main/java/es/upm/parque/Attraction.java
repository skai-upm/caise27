package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * Entry and exit work with any ticket through {@link Ticket#getUsers()}.
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
     * Admits every user of the ticket, or none if any check fails.
     *
     * @return number of users that entered
     */
    public int enter(Ticket ticket) {
        List<User> users = ticket.getUsers();
        for (User user : users) {
            if (present.contains(user)) {
                throw new ParkException("user " + user.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new ParkException("attraction " + id + " has " + getAvailable()
                    + " available places and ticket " + ticket.getId() + " needs " + users.size());
        }
        present.addAll(users);
        return users.size();
    }

    /**
     * Removes the ticket's users that are inside.
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
        if (left == 0) {
            throw new ParkException("no user of ticket " + ticket.getId() + " is in attraction " + id);
        }
        return left;
    }
}
