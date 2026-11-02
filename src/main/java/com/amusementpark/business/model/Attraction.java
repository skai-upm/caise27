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

    /** Passes an entry would consume, or why the entry is not possible. */
    protected record PassSelection(String error, List<Pass> passes) {

        static PassSelection of(List<Pass> passes) {
            return new PassSelection(null, passes);
        }

        static PassSelection failed(String reason) {
            return new PassSelection(reason, List.of());
        }
    }

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

    /**
     * Admits every holder of the ticket, or nobody. With {@code speed}, one SpeedPass per admitted
     * ticket is required and consumed; passes are consumed only when the entry succeeds.
     */
    public EntryResult enter(Admission ticket, LocalDate today, boolean speed) {
        if (!accepts(ticket)) {
            return EntryResult.rejected(getType() + " attraction " + id + " does not accept ticket "
                    + ticket.getId() + ".");
        }
        return enterChecked(admittedType.cast(ticket), today, speed);
    }

    /** Runs every access check on the admitted ticket type before changing any state. */
    private EntryResult enterChecked(T ticket, LocalDate today, boolean speed) {
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
        PassSelection selection = selectPasses(ticket, speed);
        if (selection.error() != null) {
            return EntryResult.rejected(selection.error());
        }
        selection.passes().forEach(Pass::consume);
        visitors.addAll(holders);
        return EntryResult.admitted(holders, selection.passes());
    }

    /**
     * Chooses the passes the entry needs without changing them. By default nothing is needed, and
     * SPEED requires an unused SpeedPass for every individual admission of the ticket.
     */
    protected PassSelection selectPasses(T ticket, boolean speed) {
        if (!speed) {
            return PassSelection.of(List.of());
        }
        List<Pass> passes = new ArrayList<>();
        for (IndividualAdmission admission : ticket.getAdmissions()) {
            SpeedPass pass = admission.nextUnusedSpeedPass();
            if (pass == null) {
                return PassSelection.failed("Ticket " + admission.getId() + " has no unused SpeedPass.");
            }
            passes.add(pass);
        }
        return PassSelection.of(passes);
    }

    /** Reverts an entry that could not be saved: its users leave and its passes become unused. */
    public void undo(EntryResult entry) {
        visitors.removeAll(entry.admitted());
        entry.consumedPasses().forEach(Pass::release);
    }

    /** Removes the given users that are present; returns how many left. */
    public int exit(Collection<User> users) {
        int before = visitors.size();
        visitors.removeAll(users);
        return before - visitors.size();
    }
}
