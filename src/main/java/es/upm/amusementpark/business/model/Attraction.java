package es.upm.amusementpark.business.model;

import static es.upm.amusementpark.business.rules.Checks.require;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently on it.
 * The type parameter bounds the ticket kinds the attraction admits: a ticket
 * enters only if it is an instance of {@code T}, and the subclass then works
 * with it through that capability.
 *
 * @param <T> access capability required to enter
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> visitors = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
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

    public List<User> getVisitors() {
        return Collections.unmodifiableList(visitors);
    }

    public int getAvailablePlaces() {
        return capacity - visitors.size();
    }

    public boolean isPresent(User user) {
        return visitors.contains(user);
    }

    /** Whether tickets of this kind may be used here. */
    public boolean admits(AccessTicket ticket) {
        return admitted.isInstance(ticket);
    }

    /**
     * Admits every user of the ticket, or nobody: the ticket kind, its date,
     * the users already present, the capacity and the passes required are all
     * checked before anything changes. Returns the passes consumed.
     *
     * @param speed whether the SpeedPass fast lane is requested
     */
    public List<Pass> enter(AccessTicket ticket, LocalDate today, boolean speed) {
        require(admits(ticket), getType() + " attraction " + id + " does not admit ticket " + ticket.getId());
        T typed = admitted.cast(ticket);
        require(typed.isValidOn(today), "ticket " + typed.getId() + " is not valid on " + today);
        List<User> users = new ArrayList<>();
        for (IndividualTicket access : typed.getAccessTickets()) {
            users.add(access.getUser());
        }
        for (User user : users) {
            require(!isPresent(user), "user " + user.getEmail() + " is already in " + id);
        }
        require(users.size() <= getAvailablePlaces(), "not enough places in " + id
                + " (" + getAvailablePlaces() + " available, " + users.size() + " requested)");
        List<Pass> passes = passesToConsume(typed, speed);
        visitors.addAll(users);
        passes.forEach(Pass::consume);
        return passes;
    }

    /**
     * Passes the entry will consume; rejects the entry if one is missing.
     * By default SPEED needs one unused SpeedPass for every admitted ticket.
     */
    protected List<Pass> passesToConsume(T ticket, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (speed) {
            for (IndividualTicket access : ticket.getAccessTickets()) {
                SpeedPass pass = access.findUnusedSpeedPass();
                require(pass != null, "ticket " + access.getId() + " has no unused SpeedPass");
                passes.add(pass);
            }
        }
        return passes;
    }

    public void leave(User user) {
        visitors.remove(user);
    }
}
