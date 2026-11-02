package com.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. The type parameter
 * bounds which ticket access capability the attraction admits; entry checks run on that type.
 *
 * @param <T> the admitted ticket capability
 */
public abstract class Attraction<T extends Admission> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admittedType;
    private final List<User> visitors = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admittedType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admittedType = admittedType;
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

    public List<User> getVisitors() {
        return Collections.unmodifiableList(visitors);
    }

    public int getAvailable() {
        return capacity - visitors.size();
    }

    public boolean accepts(Admission ticket) {
        return admittedType.isInstance(ticket);
    }

    /** Admits every holder of the ticket, or nobody. */
    public EntryResult enter(Admission ticket, LocalDate today) {
        if (!accepts(ticket)) {
            return EntryResult.rejected(getType() + " attraction " + id + " does not accept ticket "
                    + ticket.getId() + ".");
        }
        return enterChecked(admittedType.cast(ticket), today);
    }

    /** Runs every access check on the admitted ticket type before changing any state. */
    protected EntryResult enterChecked(T ticket, LocalDate today) {
        if (!ticket.isValidOn(today)) {
            return EntryResult.rejected("Ticket " + ticket.getId() + " is valid on " + ticket.getDate()
                    + ", not on " + today + ".");
        }
        List<User> holders = ticket.getHolders();
        for (User user : holders) {
            if (visitors.contains(user)) {
                return EntryResult.rejected(user.getEmail() + " is already in " + id + ".");
            }
        }
        if (holders.size() > getAvailable()) {
            return EntryResult.rejected("Not enough places in " + id + ": " + holders.size() + " needed, "
                    + getAvailable() + " available.");
        }
        visitors.addAll(holders);
        return EntryResult.admitted(holders, List.of());
    }

    /** Removes the given users that are present; returns how many left. */
    public int exit(Collection<User> users) {
        int before = visitors.size();
        visitors.removeAll(users);
        return before - visitors.size();
    }
}
