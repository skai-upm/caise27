package es.upm.etsisi.park.business.model.attraction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.Access;
import es.upm.etsisi.park.business.model.ticket.GroupAccess;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.user.User;

/**
 * A park attraction with a maximum capacity. It knows the users currently
 * inside it; users hold no reference back to the attraction.
 * <p>
 * The type parameter is the access capability the attraction accepts:
 * only tickets implementing {@code T} may enter, and the pass rules of each
 * subclass operate on {@code T}.
 *
 * @param <T> accepted access capability
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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getAvailable() {
        return capacity - occupants.size();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /** Whether tickets with this access capability may be used here at all. */
    public boolean accepts(Access access) {
        return accessType.isInstance(access);
    }

    /**
     * Lets every holder of {@code access} in and consumes the required
     * passes. All checks run before any change, so a failure leaves both
     * the attraction and the passes untouched.
     */
    public final Entry enter(Access access, boolean speed, LocalDate today) throws OperationNotAllowedException {
        if (!accepts(access)) {
            throw new OperationNotAllowedException(getType() + " attraction " + id + " does not accept "
                    + (access instanceof GroupAccess ? "group" : "individual") + " tickets");
        }
        T accepted = accessType.cast(access);
        if (!accepted.isValidOn(today)) {
            throw new OperationNotAllowedException("Ticket " + accepted.getId() + " is not valid on " + today);
        }
        List<Pass> passes = passesToConsume(accepted, speed);
        List<User> entering = accepted.getHolders();
        if (entering.size() > getAvailable()) {
            throw new OperationNotAllowedException("Attraction " + id + " has " + getAvailable()
                    + " free place(s) but " + entering.size() + " user(s) need to enter");
        }
        occupants.addAll(entering);
        for (Pass pass : passes) {
            pass.consume();
        }
        return new Entry(entering, passes);
    }

    /**
     * Passes that entry with {@code access} will consume. Must not modify
     * anything; throws if a required pass is missing or the option is not
     * allowed here.
     */
    protected abstract List<Pass> passesToConsume(T access, boolean speed) throws OperationNotAllowedException;

    /** Shared SpeedPass rule: with SPEED, one unconsumed SpeedPass per individual ticket entering. */
    protected static List<Pass> speedPasses(Access access, boolean speed) throws OperationNotAllowedException {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualTicket ticket : access.getIndividualTickets()) {
            SpeedPass pass = ticket.findUnconsumedSpeedPass();
            if (pass == null) {
                throw new OperationNotAllowedException("Ticket " + ticket.getId() + " has no unconsumed SpeedPass");
            }
            passes.add(pass);
        }
        return passes;
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }

    public User findOccupantByEmail(String email) {
        for (User user : occupants) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }
}
