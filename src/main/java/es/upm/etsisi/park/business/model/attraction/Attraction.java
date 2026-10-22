package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.access.Access;
import es.upm.etsisi.park.business.model.pass.Pass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A park attraction that handles accesses of type {@code T}. It knows the users currently
 * inside; users do not know it.
 *
 * @param <T> the access capability this attraction accepts
 */
public abstract class Attraction<T extends Access> {

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

    public abstract AttractionType getType();

    /** The access as {@code T} when this attraction accepts it. */
    public Optional<T> admit(Access access) {
        return accessType.isInstance(access) ? Optional.of(accessType.cast(access)) : Optional.empty();
    }

    /**
     * Admits every user of the access, or nobody. All checks run before any state changes.
     *
     * @return the passes consumed by this entry
     */
    public List<Pass> enter(T access, boolean speed, PassLookup passes) {
        List<Pass> toConsume = requiredPasses(access, speed, passes);
        List<User> users = access.getUsers();
        if (users.size() > getAvailablePlaces()) {
            throw new RuleViolationException("Not enough capacity in " + id + ": " + getAvailablePlaces()
                    + " places available, " + users.size() + " needed");
        }
        occupants.addAll(users);
        toConsume.forEach(Pass::consume);
        return toConsume;
    }

    /**
     * Passes that must be consumed for this entry. By default none, or one unconsumed
     * SpeedPass per admitted ticket when {@code speed} is requested.
     */
    protected List<Pass> requiredPasses(T access, boolean speed, PassLookup passes) {
        List<Pass> required = new ArrayList<>();
        if (speed) {
            for (IndividualTicket ticket : access.getAdmittedTickets()) {
                required.add(passes.availableSpeedPass(ticket).orElseThrow(() -> new RuleViolationException(
                        "Ticket " + ticket.getId() + " has no unconsumed SpeedPass")));
            }
        }
        return required;
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }
}
