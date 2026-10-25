package es.upm.parque;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. Entry and exit work with any
 * {@link Ticket}, so individual and group tickets follow the same logic.
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
        return List.copyOf(present);
    }

    public int getAvailablePlaces() {
        return capacity - present.size();
    }

    /**
     * Lets in every user of the ticket, or nobody if any of them is already inside or they do not all fit.
     *
     * @return the number of users that entered
     * @throws IllegalStateException with the reason when the entry is rejected
     */
    public int enter(Ticket ticket) {
        List<User> users = ticket.getUsers();
        if (users.isEmpty()) {
            throw new IllegalStateException("Ticket " + ticket.getId() + " has no users");
        }
        for (User user : users) {
            if (present.contains(user)) {
                throw new IllegalStateException(user.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new IllegalStateException("Not enough capacity in " + id + ": " + getAvailablePlaces()
                    + " available, " + users.size() + " needed");
        }
        present.addAll(users);
        return users.size();
    }

    /**
     * Lets out the users of the ticket that are inside.
     *
     * @return the number of users that left
     * @throws IllegalStateException if none of the ticket's users is inside
     */
    public int exit(Ticket ticket) {
        int before = present.size();
        present.removeAll(ticket.getUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalStateException("No user of ticket " + ticket.getId() + " is in attraction " + id);
        }
        return left;
    }
}
