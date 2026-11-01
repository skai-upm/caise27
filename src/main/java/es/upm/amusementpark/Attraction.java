package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity. The users present are runtime
 * state only and start empty every execution.
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

    public int getAvailablePlaces() {
        return capacity - visitors.size();
    }

    /** Admits every user of the ticket, or nobody if any of them cannot enter. */
    public void enter(Ticket ticket) {
        List<User> entering = ticket.getUsers();
        for (User user : entering) {
            if (visitors.contains(user)) {
                throw new OperationException(user.getEmail() + " is already in attraction " + id);
            }
        }
        if (entering.size() > getAvailablePlaces()) {
            throw new OperationException("attraction " + id + " has " + getAvailablePlaces()
                    + " available places but the ticket admits " + entering.size());
        }
        visitors.addAll(entering);
    }

    /** Removes the ticket's users who are present and returns how many left. */
    public int exit(Ticket ticket) {
        List<User> leaving = new ArrayList<>();
        for (User user : ticket.getUsers()) {
            if (visitors.contains(user)) {
                leaving.add(user);
            }
        }
        if (leaving.isEmpty()) {
            throw new OperationException("no user of ticket " + ticket.getId() + " is in attraction " + id);
        }
        visitors.removeAll(leaving);
        return leaving.size();
    }

    public void removeVisitor(User user) {
        visitors.remove(user);
    }
}
