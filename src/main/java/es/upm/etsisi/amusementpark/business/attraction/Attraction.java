package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.AccessTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction that accepts tickets with access capability {@code T}. It knows the users
 * currently inside; users do not know attractions.
 *
 * @param <T> the access capability a ticket must have to be used here
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accepted) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getFreePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /** Whether tickets of this kind may be used here, as decided by the type parameter. */
    public boolean accepts(AccessTicket ticket) {
        return accepted.isInstance(ticket);
    }

    /**
     * Checks every entry condition without changing any state.
     *
     * @return who would enter and which passes would be consumed
     */
    public Entry prepareEntry(AccessTicket ticket, boolean speed, LocalDate today, PassBook passes)
            throws BusinessException {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException(getType() + " attraction " + id
                    + " does not accept ticket " + ticket.getId());
        }
        T typed = accepted.cast(ticket);
        if (!typed.isValidOn(today)) {
            throw new OperationNotAllowedException("ticket " + ticket.getId() + " is not valid on " + today);
        }
        List<Pass> toConsume = selectPasses(typed, speed, passes);
        List<User> entrants = typed.getIndividualTickets().stream().map(IndividualTicket::getUser).toList();
        if (entrants.stream().anyMatch(this::isInside)) {
            throw new OperationNotAllowedException("a ticket holder is already inside " + id);
        }
        if (entrants.size() > getFreePlaces()) {
            throw new OperationNotAllowedException("not enough places in " + id + " (" + getFreePlaces()
                    + " free, " + entrants.size() + " needed)");
        }
        return new Entry(entrants, toConsume);
    }

    /**
     * Chooses the passes an entry consumes. By default SpeedPass entry needs one unused SpeedPass
     * per individual ticket and normal entry needs none.
     */
    protected List<Pass> selectPasses(T ticket, boolean speed, PassBook passes) throws BusinessException {
        List<Pass> selected = new ArrayList<>();
        if (speed) {
            for (IndividualTicket individual : ticket.getIndividualTickets()) {
                selected.add(passes.unusedSpeedPass(individual).orElseThrow(() -> new OperationNotAllowedException(
                        "ticket " + individual.getId() + " has no unused SpeedPass")));
            }
        }
        return selected;
    }

    /** Admits all the given users together, or none of them. */
    public void admit(Collection<User> users) {
        if (users.size() > getFreePlaces() || users.stream().anyMatch(this::isInside)) {
            throw new IllegalStateException("cannot admit users to attraction " + id);
        }
        occupants.addAll(users);
    }

    /** Removes a user, returning whether they were inside. */
    public boolean release(User user) {
        return occupants.remove(user);
    }
}
