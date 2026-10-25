package es.upm.parque;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
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

    public int getAvailable() {
        return capacity - present.size();
    }

    /**
     * Lets in every user admitted by the ticket, or nobody.
     *
     * @return number of users that entered
     */
    public int enter(Ticket ticket, LocalDate today) {
        if (!ticket.isValidOn(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " is valid on " + ticket.getDate()
                    + ", not on " + today);
        }
        List<User> users = ticket.getUsers();
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " admits no users");
        }
        for (User u : users) {
            if (present.contains(u)) {
                throw new IllegalArgumentException("User " + u.getEmail() + " is already in " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new IllegalArgumentException("Not enough capacity in " + id + ": " + getAvailable()
                    + " place(s) free, " + users.size() + " required");
        }
        present.addAll(users);
        return users.size();
    }

    /**
     * Lets out the ticket's users that are inside.
     *
     * @return number of users that left
     */
    public int exit(Ticket ticket) {
        int before = present.size();
        present.removeAll(ticket.getUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalArgumentException("No user of ticket " + ticket.getId() + " is in " + id);
        }
        return left;
    }
}
