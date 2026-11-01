package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * Occupancy is runtime state only.
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

    public List<User> getPresentUsers() {
        return List.copyOf(present);
    }

    public int getOccupancy() {
        return present.size();
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    /**
     * Admits every user of the ticket, or nobody if any check fails.
     */
    public void enter(Ticket ticket, LocalDate today) throws OperationException {
        if (!ticket.isValidOn(today)) {
            throw new OperationException("Ticket " + ticket.getId() + " is for " + ticket.getDate()
                    + ", not for the current date " + today);
        }
        List<User> entering = ticket.getUsers();
        for (User u : entering) {
            if (present.contains(u)) {
                throw new OperationException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (entering.size() > getAvailable()) {
            throw new OperationException("Not enough capacity in attraction " + id + ": " + getAvailable()
                    + " place(s) available, " + entering.size() + " needed");
        }
        present.addAll(entering);
    }

    /**
     * Lets the ticket's users who are inside leave. The ticket date is not checked.
     *
     * @return number of users who left
     */
    public int exit(Ticket ticket) throws OperationException {
        int left = 0;
        for (User u : ticket.getUsers()) {
            if (present.remove(u)) {
                left++;
            }
        }
        if (left == 0) {
            throw new OperationException("No user of ticket " + ticket.getId() + " is in attraction " + id);
        }
        return left;
    }

    public void removeUser(User user) {
        present.remove(user);
    }
}
