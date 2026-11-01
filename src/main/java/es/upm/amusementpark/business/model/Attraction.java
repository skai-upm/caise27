package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * {@code T} bounds the kind of ticket access the attraction handles; tickets of
 * other kinds are rejected. Occupancy is runtime state only.
 *
 * @param <T> access capability admitted by this attraction
 */
public abstract class Attraction<T extends AttractionAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accessType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
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

    public abstract AttractionType getType();

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
     * True if this attraction handles the given kind of ticket.
     */
    public boolean admits(Ticket ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Admits every user of the ticket, or nobody if any check fails. With {@code speed}
     * one unused SpeedPass of each admitted ticket is consumed; subclasses may require
     * other passes. Passes are consumed only when entry succeeds.
     *
     * @return the passes consumed by this entry
     */
    public List<Pass> enter(Ticket ticket, boolean speed, LocalDate today) throws OperationException {
        if (!admits(ticket)) {
            throw new OperationException("Attraction " + id + " (" + getType() + ") does not admit ticket "
                    + ticket.getId());
        }
        T access = accessType.cast(ticket);
        if (!access.isValidOn(today)) {
            throw new OperationException("Ticket " + ticket.getId() + " is for " + ticket.getDate()
                    + ", not for the current date " + today);
        }
        List<User> entering = access.getUsers();
        for (User u : entering) {
            if (present.contains(u)) {
                throw new OperationException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (entering.size() > getAvailable()) {
            throw new OperationException("Not enough capacity in attraction " + id + ": " + getAvailable()
                    + " place(s) available, " + entering.size() + " needed");
        }
        List<Pass> passes = passesToConsume(access, speed);
        present.addAll(entering);
        for (Pass pass : passes) {
            pass.consume();
        }
        return passes;
    }

    /**
     * Passes the entry will consume; throws if a required pass is missing. Nothing is
     * modified here. By default SPEED needs one unused SpeedPass per admitted ticket.
     */
    protected List<Pass> passesToConsume(T access, boolean speed) throws OperationException {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualTicket t : access.getAccessTickets()) {
            SpeedPass pass = t.getUnusedSpeedPass();
            if (pass == null) {
                throw new OperationException("Ticket " + t.getId() + " has no unused SpeedPass");
            }
            passes.add(pass);
        }
        return passes;
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
