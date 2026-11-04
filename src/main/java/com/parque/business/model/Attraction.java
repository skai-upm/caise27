package com.parque.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently present, generic in the ticket access
 * capability it admits. The bound {@code T extends Admission} plus the accepted capability class decide which
 * tickets are admitted, and the typed {@link #enter} receives them as {@code T}. Occupancy is not persisted.
 *
 * @param <T> access capability admitted by this attraction
 */
public abstract class Attraction<T extends Admission> {
    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> acceptedType;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> acceptedType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.acceptedType = acceptedType;
    }

    public abstract AttractionType getType();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Users currently present (read-only view). */
    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int available() {
        return capacity - present.size();
    }

    public boolean isPresent(User user) {
        for (User u : present) {
            if (u == user) {
                return true;
            }
        }
        return false;
    }

    /** True if tickets with this capability are admitted by this attraction type. */
    public boolean accepts(Admission ticket) {
        return acceptedType.isInstance(ticket);
    }

    /** Admits any ticket whose capability this attraction accepts; others are rejected. */
    public void admit(Admission ticket, LocalDate today) {
        if (!accepts(ticket)) {
            throw new IllegalArgumentException(getType() + " attraction " + id + " does not admit ticket "
                    + ticket.getId());
        }
        enter(acceptedType.cast(ticket), today);
    }

    /**
     * Admits all visitors of the ticket or none: the ticket must be for {@code today}, the type-specific
     * conditions must hold, none of its visitors may already be inside, and they must all fit.
     */
    public void enter(T ticket, LocalDate today) {
        if (!ticket.getDate().equals(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " is valid for " + ticket.getDate()
                    + ", not for the system date " + today);
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.isEmpty()) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " has no registered visitors");
        }
        for (User v : visitors) {
            if (isPresent(v)) {
                throw new IllegalArgumentException("User " + v.getEmail() + " is already in attraction " + id);
            }
        }
        if (visitors.size() > available()) {
            throw new IllegalArgumentException("Not enough places in attraction " + id + ": " + visitors.size()
                    + " needed, " + available() + " available");
        }
        checkAdditionalConditions(ticket, today);
        present.addAll(visitors);
    }

    /** Extra admission conditions of a type; must not change state when it rejects. None by default. */
    protected void checkAdditionalConditions(T ticket, LocalDate today) {
    }

    /** Removes the ticket's visitors that are present; returns how many left. Rejects if none was present. */
    public int exit(Admission ticket) {
        int removed = 0;
        for (User v : ticket.getVisitors()) {
            if (present.removeIf(u -> u == v)) {
                removed++;
            }
        }
        if (removed == 0) {
            throw new IllegalArgumentException("No visitor of ticket " + ticket.getId() + " is in attraction " + id);
        }
        return removed;
    }
}
