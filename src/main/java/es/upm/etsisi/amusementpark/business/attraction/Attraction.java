package es.upm.etsisi.amusementpark.business.attraction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.PassBook;
import es.upm.etsisi.amusementpark.business.ticket.AccessTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * A park attraction and the users currently inside it. The type parameter is the
 * access capability of the tickets the attraction handles; entry only proceeds with
 * a ticket of that type. Users hold no reference back to the attraction, and
 * occupancy lives only in memory.
 *
 * @param <T> the ticket access capability this attraction accepts
 */
public abstract class Attraction<T extends AccessTicket> {
    private final String identifier;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String identifier, String name, int capacity, Class<T> accepted) {
        this.identifier = identifier;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
    }

    public abstract AttractionType getType();

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.stream().anyMatch(o -> o == user);
    }

    public boolean accepts(AccessTicket ticket) {
        return accepted.isInstance(ticket);
    }

    /**
     * Checks every entry condition first and only then admits the ticket's users and
     * consumes the selected passes. A rejected entry changes nothing.
     */
    public EntryResult enter(AccessTicket ticket, boolean speed, LocalDate today, PassBook passes)
            throws RuleViolationException {
        if (!accepts(ticket)) {
            throw new RuleViolationException(getType() + " attraction " + identifier + " does not accept ticket "
                    + ticket.getIdentifier());
        }
        T admitted = accepted.cast(ticket);
        if (!admitted.isValidOn(today)) {
            throw new RuleViolationException("ticket " + admitted.getIdentifier() + " is not valid on " + today);
        }
        List<User> entrants = admitted.getUsers();
        for (User user : entrants) {
            if (isInside(user)) {
                throw new RuleViolationException(user.getEmail() + " is already inside " + identifier);
            }
        }
        if (entrants.size() > getAvailablePlaces()) {
            throw new RuleViolationException("not enough places in " + identifier + ": " + getAvailablePlaces()
                    + " available, " + entrants.size() + " needed");
        }
        List<Pass> consumed = selectPasses(admitted, speed, passes);
        occupants.addAll(entrants);
        consumed.forEach(p -> p.setConsumed(true));
        return new EntryResult(entrants, consumed);
    }

    /**
     * Passes this entry consumes. By default one unused SpeedPass per entering
     * individual ticket when SPEED is requested, none otherwise.
     */
    protected List<Pass> selectPasses(T ticket, boolean speed, PassBook passes) throws RuleViolationException {
        if (!speed) {
            return List.of();
        }
        List<Pass> selected = new ArrayList<>();
        for (IndividualTicket entering : ticket.getEntryTickets()) {
            selected.add(passes.unusedSpeedPass(entering).orElseThrow(() -> new RuleViolationException(
                    "ticket " + entering.getIdentifier() + " has no unused SpeedPass")));
        }
        return selected;
    }

    /**
     * Reverts a successful entry (used when its consequences cannot be saved).
     */
    public void undo(EntryResult entry) {
        entry.users().forEach(this::release);
        entry.consumedPasses().forEach(p -> p.setConsumed(false));
    }

    public boolean release(User user) {
        return occupants.removeIf(o -> o == user);
    }

    /**
     * Admits users without checks; used only to restore occupancy during a rollback.
     */
    public void restore(User user) {
        occupants.add(user);
    }
}
