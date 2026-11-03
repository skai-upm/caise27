package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently present (never persisted).
 * The type parameter is the ticket access capability the attraction admits: {@link #admit} only accepts
 * tickets of that capability, which the compiler enforces; tickets whose kind is only known at run time
 * go through {@link #enter}, which checks the capability before admitting.
 *
 * @param <T> access capability admitted by this attraction
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
    }

    public abstract AttractionType getType();

    /** True if tickets of this kind may be used at this attraction. */
    public boolean admits(AccessTicket ticket) {
        return admitted.isInstance(ticket);
    }

    /** Whether entries may request SpeedPass use. */
    public boolean acceptsSpeed() {
        return true;
    }

    /** Whether every entry needs, and consumes, the ticket's Special Pass. */
    public boolean requiresSpecialPass() {
        return false;
    }

    /**
     * Checks, without changing anything, every attraction condition of an entry: the ticket kind is admitted,
     * it is dated {@code today}, none of its users is already present and all of them fit.
     */
    public void requireAdmissible(AccessTicket ticket, LocalDate today) {
        if (!admits(ticket)) {
            throw new IllegalArgumentException("Attraction " + id + " (" + getType() + ") does not admit ticket "
                    + ticket.getId());
        }
        check(ticket, today);
    }

    /** Entry with a ticket whose kind is only known at run time: rejected if this attraction does not admit it. */
    public void enter(AccessTicket ticket, LocalDate today) {
        requireAdmissible(ticket, today);
        admit(admitted.cast(ticket), today);
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

    /** Users currently present, in order of entry. */
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
     * Admits every user of the ticket, or none: the ticket must be dated {@code today}, no user may already be
     * present and all of them must fit in the remaining capacity.
     */
    public void admit(T ticket, LocalDate today) {
        check(ticket, today);
        present.addAll(ticket.getVisitors());
    }

    private void check(AccessTicket ticket, LocalDate today) {
        if (!ticket.getDate().equals(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " is not valid on " + today
                    + " (it is dated " + ticket.getDate() + ")");
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.isEmpty()) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " has no registered users");
        }
        for (User u : visitors) {
            if (isPresent(u)) {
                throw new IllegalArgumentException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (visitors.size() > getAvailable()) {
            throw new IllegalArgumentException("Not enough capacity in attraction " + id + ": " + visitors.size()
                    + " users, " + getAvailable() + " places available");
        }
    }

    /** Removes the given users that are present; returns how many left. */
    public int exit(Collection<User> users) {
        int before = present.size();
        present.removeIf(p -> users.stream().anyMatch(u -> u == p));
        return before - present.size();
    }
}
