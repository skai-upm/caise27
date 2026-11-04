package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A park attraction with a maximum capacity and the users currently present.
 * {@code T} is the ticket access capability the attraction accepts: the typed
 * {@link #enter}/{@link #exit} operations only take such tickets.
 *
 * @param <T> accepted ticket capability
 */
public abstract class Attraction<T extends Admissible> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final Set<User> present = new LinkedHashSet<>();

    protected Attraction(String id, String name, int capacity, Class<T> accepted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
    }

    /** The attraction type, as shown and stored. */
    public abstract AttractionType type();

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
        return Collections.unmodifiableList(new ArrayList<>(present));
    }

    public int getPresentCount() {
        return present.size();
    }

    public int getAvailablePlaces() {
        return capacity - present.size();
    }

    /** True if this attraction's type accepts {@code ticket}'s kind. */
    public boolean accepts(Admissible ticket) {
        return accepted.isInstance(ticket);
    }

    /**
     * Admits every holder of {@code ticket}, or none: the ticket must be dated
     * {@code today}, no holder may already be present and capacity must suffice.
     */
    public void enter(T ticket, LocalDate today) {
        requireValid(ticket.getDate().equals(today),
                "ticket " + ticket.getId() + " is valid for " + ticket.getDate() + ", not for " + today);
        List<User> holders = ticket.getHolders();
        requireValid(!holders.isEmpty() && !holders.contains(null),
                "ticket " + ticket.getId() + " has no current holders");
        for (User u : holders) {
            requireValid(!present.contains(u), "user " + u.getEmail() + " is already in attraction " + id);
        }
        requireValid(holders.size() <= getAvailablePlaces(), "not enough capacity in attraction " + id
                + ": " + holders.size() + " requested, " + getAvailablePlaces() + " available");
        beforeAdmission(ticket);
        present.addAll(holders);
    }

    /** Removes the holders of {@code ticket} that are present; returns how many left. */
    public int exit(T ticket) {
        int removed = 0;
        for (User u : ticket.getHolders()) {
            if (u != null && present.remove(u)) {
                removed++;
            }
        }
        requireValid(removed > 0, "no holder of ticket " + ticket.getId() + " is in attraction " + id);
        return removed;
    }

    /** Entry for a ticket whose kind is only known at runtime (e.g. resolved from a command). */
    public void enterAny(Admissible ticket, LocalDate today) {
        enter(requireAccepted(ticket), today);
    }

    /** Exit for a ticket whose kind is only known at runtime. */
    public int exitAny(Admissible ticket) {
        return exit(requireAccepted(ticket));
    }

    /** Hook for type-specific checks run after the common checks and before admission. */
    protected void beforeAdmission(T ticket) {
        // no additional rules by default
    }

    private T requireAccepted(Admissible ticket) {
        requireValid(accepts(ticket), "attraction " + id + " (" + type() + ") does not accept "
                + (ticket instanceof GroupAccess ? "group" : "individual") + " tickets");
        return accepted.cast(ticket);
    }

    protected static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
