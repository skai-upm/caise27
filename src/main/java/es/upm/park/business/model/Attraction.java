package es.upm.park.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * The type parameter is the ticket capability the attraction accepts: only
 * tickets implementing {@code T} are admissible, and the attraction works on
 * them through {@code T}'s operations. Only the attraction references its
 * occupants; users know nothing of it.
 *
 * @param <T> accepted ticket capability
 */
public abstract class Attraction<T extends Admission> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accepted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
    }

    public abstract AttractionType getType();

    /** The ticket as this attraction's capability, if it implements it. */
    public Optional<T> admissible(Admission ticket) {
        return accepted.isInstance(ticket) ? Optional.of(accepted.cast(ticket)) : Optional.empty();
    }

    /** Users that enter with the ticket. */
    public List<User> holdersOf(T ticket) {
        return ticket.holders();
    }

    /** Whether entry with SpeedPasses is allowed here. */
    public boolean acceptsSpeedPasses() {
        return true;
    }

    /**
     * Individual tickets that must each spend a SpeedPass when the ticket
     * enters with the SPEED option.
     */
    public List<IndividualAccess> speedPassTickets(T ticket) {
        if (ticket instanceof GroupAccess group) {
            return List.copyOf(group.getComposition());
        }
        if (ticket instanceof IndividualAccess individual) {
            return List.of(individual);
        }
        return List.of();
    }

    /** Passes consumed automatically when the ticket enters. */
    public List<Pass> automaticPasses(T ticket, PassLookup passes) {
        return List.of();
    }

    /** Reason why the ticket cannot enter beyond type and capacity, if any. */
    public Optional<String> missingRequirement(T ticket, PassLookup passes) {
        return Optional.empty();
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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /** Lets a user in; returns false if already inside or full. */
    public boolean enter(User user) {
        if (contains(user) || isFull()) {
            return false;
        }
        return occupants.add(user);
    }

    public boolean leave(User user) {
        return occupants.remove(user);
    }
}
