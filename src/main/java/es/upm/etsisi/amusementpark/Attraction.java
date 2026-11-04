package es.upm.etsisi.amusementpark;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * Entry and exit work on the {@link Ticket} abstraction only, so individual and
 * group tickets are handled by the same code.
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

    public boolean isPresent(User user) {
        return present.stream().anyMatch(u -> u == user);
    }

    /**
     * Admits all users of the ticket, or none: the ticket must be for {@code today},
     * none of its users may already be inside, and all must fit.
     */
    public void enter(Ticket ticket, LocalDate today) throws UserException {
        if (!ticket.getDate().equals(today)) {
            throw new UserException("Ticket " + ticket.getId() + " is for " + ticket.getDate()
                    + ", not for " + today);
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.isEmpty()) {
            throw new UserException("Ticket " + ticket.getId() + " has no existing users");
        }
        for (User v : visitors) {
            if (isPresent(v)) {
                throw new UserException("User " + v.getEmail() + " is already in attraction " + id);
            }
        }
        if (visitors.size() > getAvailable()) {
            throw new UserException("Attraction " + id + " has " + getAvailable() + " free places; ticket "
                    + ticket.getId() + " needs " + visitors.size());
        }
        present.addAll(visitors);
    }

    /** Removes the ticket's users that are inside; fails if none of them is. */
    public void exit(Ticket ticket) throws UserException {
        List<User> leaving = new ArrayList<>();
        for (User v : ticket.getVisitors()) {
            if (isPresent(v)) {
                leaving.add(v);
            }
        }
        if (leaving.isEmpty()) {
            throw new UserException("Nobody with ticket " + ticket.getId() + " is in attraction " + id);
        }
        present.removeIf(u -> leaving.stream().anyMatch(l -> l == u));
    }

    /** Removes a (deleted) user if present. */
    public void remove(User user) {
        present.removeIf(u -> u == user);
    }
}
