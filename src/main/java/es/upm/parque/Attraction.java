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
    private final List<User> users = new ArrayList<>();

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

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public int getAvailable() {
        return capacity - users.size();
    }

    /**
     * Lets in every user of the ticket, or nobody. Returns null on success or the reason for rejection.
     */
    public String enter(Ticket ticket, LocalDate currentDate) {
        if (!ticket.getDate().equals(currentDate)) {
            return "ticket " + ticket.getId() + " is not valid for " + currentDate;
        }
        List<User> entering = ticket.getUsers();
        for (User u : entering) {
            if (users.contains(u)) {
                return "user " + u.getEmail() + " is already in the attraction";
            }
        }
        if (entering.size() > getAvailable()) {
            return "not enough capacity (" + getAvailable() + " available, " + entering.size() + " required)";
        }
        users.addAll(entering);
        return null;
    }

    /**
     * Removes the users of the ticket that are inside. Returns how many left.
     */
    public int exit(Ticket ticket) {
        int before = users.size();
        users.removeAll(ticket.getUsers());
        return before - users.size();
    }
}
