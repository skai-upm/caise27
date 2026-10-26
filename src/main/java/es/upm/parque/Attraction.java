package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside it. Access works the same for every
 * ticket kind because it only relies on the users each ticket admits.
 */
public class Attraction {
    private final String id;
    private String name;
    private int capacity;
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
     * Lets every user of the ticket in, or nobody: all checks are done before the attraction changes.
     */
    public void enter(Ticket ticket) {
        List<User> users = ticket.getUsers();
        for (User u : users) {
            if (present.contains(u)) {
                throw new IllegalArgumentException(u.getDni() + " is already inside attraction " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new IllegalArgumentException("attraction " + id + " has " + getAvailable() + " free places but ticket "
                    + ticket.getId() + " needs " + users.size());
        }
        present.addAll(users);
    }

    /** Removes the ticket's users that are inside and returns how many left. */
    public int exit(Ticket ticket) {
        int before = present.size();
        present.removeAll(ticket.getUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalArgumentException("no user of ticket " + ticket.getId() + " is inside attraction " + id);
        }
        return left;
    }

    @Override
    public String toString() {
        return id + " (" + name + ", " + present.size() + "/" + capacity + ")";
    }
}
