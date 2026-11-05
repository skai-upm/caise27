package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside it. The type parameter is the ticket
 * access capability the attraction handles: only tickets with that capability can be obtained through
 * {@link #accept} and checked by {@link #passesToConsume}. The attraction knows its occupants; users do not know
 * the attraction they are in.
 *
 * @param <T> access capability accepted by this attraction
 */
public abstract class Attraction<T extends GeneralAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> occupants = new ArrayList<>();

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

    /** The ticket typed as this attraction's capability, or {@code null} when this attraction does not accept it. */
    public T accept(GeneralAccess ticket) {
        return accessType.isInstance(ticket) ? accessType.cast(ticket) : null;
    }

    /** Whether entry with SpeedPasses ({@code SPEED}) is allowed; special attractions manage their own queue. */
    public boolean acceptsSpeedPasses() {
        return true;
    }

    /**
     * Pass conditions of an entry with an accepted ticket, for the individual tickets of the users who enter:
     * with {@code speed}, one unconsumed SpeedPass of each of them (the first available one), otherwise none.
     * Nothing is consumed here; the caller consumes the selected passes only when the whole entry succeeds.
     */
    public PassSelection passesToConsume(T ticket, boolean speed, List<IndividualTicket> entering,
            PassLookup passes) {
        if (!speed) {
            return PassSelection.ok(List.of());
        }
        if (!acceptsSpeedPasses()) {
            return PassSelection.refused(getType() + " attraction " + id + " does not accept SpeedPasses");
        }
        List<Pass> selected = new ArrayList<>();
        for (IndividualTicket individual : entering) {
            Pass speedPass = passes.passesOf(individual).stream()
                    .filter(p -> p instanceof SpeedPass && !p.isConsumed()).findFirst().orElse(null);
            if (speedPass == null) {
                return PassSelection.refused("ticket " + individual.getId() + " has no unconsumed SpeedPass;"
                        + " nobody entered");
            }
            selected.add(speedPass);
        }
        return PassSelection.ok(selected);
    }

    /** Users currently inside, in order of entry; read-only view. */
    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public boolean isInside(User user) {
        return occupants.stream().anyMatch(u -> u.getDni().equals(user.getDni()));
    }

    /** Adds the user unless already inside; returns whether it was added. */
    public boolean addOccupant(User user) {
        if (isInside(user)) {
            return false;
        }
        occupants.add(user);
        return true;
    }

    /** Removes the user if inside; returns whether it was removed. */
    public boolean removeOccupant(User user) {
        return occupants.removeIf(u -> u.getDni().equals(user.getDni()));
    }
}
