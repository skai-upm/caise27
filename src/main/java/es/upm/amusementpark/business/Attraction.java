package es.upm.amusementpark.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Park attraction accepting tickets with access capability {@code T}. It references the users currently inside
 * it (by e-mail); users never reference attractions.
 *
 * @param <T> access capability required to enter
 */
public abstract class Attraction<T extends GeneralAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final Set<String> occupants = new LinkedHashSet<>();

    protected Attraction(String id, String name, int capacity, Class<T> accessType) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.capacity = capacity;
        this.accessType = Objects.requireNonNull(accessType, "accessType");
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

    /** @return the access capability a ticket needs to enter */
    public Class<T> getAccessType() {
        return accessType;
    }

    /** @return whether the ticket has the capability this attraction is bound to */
    public boolean accepts(GeneralAccess ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Result of a successful admission check: the users to admit and the passes to consume on entry.
     *
     * @param holders e-mails of the users entering together
     * @param toConsume passes that must be marked as consumed when the entry is completed
     */
    public record Admission(List<String> holders, List<Pass> toConsume) {
    }

    /**
     * Checks, without changing any state, whether a ticket may enter this attraction and which passes the entry
     * consumes.
     *
     * @param ticket ticket used for entry
     * @param passes lookup of the passes of individual tickets
     * @param speed whether SpeedPasses are requested
     * @throws ParkException if the ticket kind, pass use or type-specific conditions are not satisfied
     */
    public Admission admit(GeneralAccess ticket, PassLookup passes, boolean speed) {
        if (!accepts(ticket)) {
            throw new ParkException("Attraction " + id + " (" + getType() + ") does not accept "
                    + (ticket instanceof GroupAccess ? "group" : "individual") + " tickets");
        }
        T typed = accessType.cast(ticket);
        List<Pass> toConsume = new ArrayList<>();
        if (speed) {
            if (!acceptsSpeedPasses()) {
                throw new ParkException("Attraction " + id + " (" + getType() + ") does not accept SpeedPasses");
            }
            for (String ticketId : typed.individualTicketIds()) {
                toConsume.add(availableSpeedPass(ticketId, passes));
            }
        }
        toConsume.addAll(requiredPasses(typed, passes));
        return new Admission(typed.holders(), List.copyOf(toConsume));
    }

    private static SpeedPass availableSpeedPass(String ticketId, PassLookup passes) {
        for (Pass pass : passes.passesOfTicket(ticketId)) {
            if (pass instanceof SpeedPass speedPass && !speedPass.isConsumed()) {
                return speedPass;
            }
        }
        throw new ParkException("Ticket " + ticketId + " has no unconsumed SpeedPass");
    }

    /** @return whether entry with SpeedPasses is allowed at this attraction */
    protected boolean acceptsSpeedPasses() {
        return true;
    }

    /**
     * Passes this attraction type requires and consumes for an admitted ticket; none by default.
     *
     * @throws ParkException if a required pass is missing
     */
    protected List<Pass> requiredPasses(T ticket, PassLookup passes) {
        return List.of();
    }

    /** @return unmodifiable view of the e-mails of the users currently inside, in order of entry */
    public Set<String> getOccupants() {
        return Collections.unmodifiableSet(occupants);
    }

    /** @return capacity minus the number of users currently inside */
    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(String email) {
        return occupants.contains(email);
    }

    /** @return {@code false} if the attraction is full or the user is already inside */
    boolean enter(String email) {
        if (isFull() || occupants.contains(email)) {
            return false;
        }
        return occupants.add(email);
    }

    /** Replaces an occupant's e-mail, keeping their presence. */
    void replaceOccupant(String oldEmail, String newEmail) {
        if (occupants.remove(oldEmail)) {
            occupants.add(newEmail);
        }
    }

    /** @return {@code false} if the user was not inside */
    boolean leave(String email) {
        return occupants.remove(email);
    }
}
