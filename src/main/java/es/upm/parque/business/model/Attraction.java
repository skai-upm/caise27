package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. Any kind of ticket
 * is handled the same way through the users it covers.
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

    public int getOccupancy() {
        return visitors.size();
    }

    public int getAvailablePlaces() {
        return capacity - visitors.size();
    }

    /**
     * Lets in every user covered by the ticket, or nobody if any is already inside or they do not
     * all fit. Returns how many users entered.
     */
    public int admit(Ticket ticket) {
        List<User> users = ticket.getUsers();
        if (users.isEmpty()) {
            throw new ParkException("Ticket " + ticket.getId() + " covers no users");
        }
        for (User u : users) {
            if (isPresent(u)) {
                throw new ParkException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new ParkException("Not enough places in attraction " + id + ": " + users.size()
                    + " requested, " + getAvailablePlaces() + " available");
        }
        visitors.addAll(users);
        return users.size();
    }

    /**
     * Lets out the users of the ticket who are inside. Returns how many left.
     */
    public int release(Ticket ticket) {
        int before = visitors.size();
        for (User u : ticket.getUsers()) {
            visitors.removeIf(v -> v == u);
        }
        return before - visitors.size();
    }

    private boolean isPresent(User user) {
        return visitors.stream().anyMatch(v -> v == user);
    }
}
