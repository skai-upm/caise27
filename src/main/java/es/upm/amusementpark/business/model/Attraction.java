package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;

/**
 * A park attraction accepting tickets with the access capability {@code T}. It knows the users
 * currently inside it; users do not know the attraction.
 *
 * @param <T> kind of ticket this attraction can handle
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String id;
    private String name;
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Maximum number of people inside at the same time.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Users currently inside, in order of entry.
     */
    public List<User> getOccupants() {
        return new ArrayList<>(occupants);
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    public void addOccupant(User user) {
        occupants.add(user);
    }

    public boolean removeOccupant(User user) {
        return occupants.remove(user);
    }

    /**
     * Tells whether tickets of this kind may be used here.
     */
    public boolean accepts(AccessTicket ticket) {
        return accepted.isInstance(ticket);
    }

    /**
     * Normal entry (no SpeedPass).
     *
     * @return the users who entered
     */
    public final List<User> enter(T ticket, LocalDate today, PassLookup passes) throws ParkException {
        return enter(ticket, today, passes, false).users();
    }

    /**
     * Lets every user of the ticket enter, or none. Every condition is checked before anything
     * changes: validity today, SpeedPass option allowed, passes required by the option or the
     * attraction type, users not already inside and enough capacity. Then all users enter and the
     * selected passes are consumed.
     */
    public final EntryResult enter(T ticket, LocalDate today, PassLookup passes, boolean speed) throws ParkException {
        if (!ticket.getDate().equals(today)) {
            throw new OperationNotAllowedException("Ticket is not valid for the current date");
        }
        if (speed && !acceptsSpeedPasses()) {
            throw new OperationNotAllowedException(getType() + " attractions do not accept SpeedPasses");
        }
        List<Pass> toConsume = passesToConsume(ticket, passes, speed);
        List<User> entering = ticket.individualTickets().stream().map(IndividualTicket::getUser).toList();
        for (User user : entering) {
            if (contains(user)) {
                throw new ConflictException("User is already inside the attraction: " + user.getEmail());
            }
        }
        if (occupants.size() + entering.size() > capacity) {
            throw new OperationNotAllowedException("Not enough capacity in the attraction");
        }
        occupants.addAll(entering);
        toConsume.forEach(Pass::consume);
        return new EntryResult(entering, toConsume);
    }

    /**
     * Entry for callers holding any ticket: rejected unless this attraction accepts its kind.
     */
    public EntryResult enterAny(AccessTicket ticket, LocalDate today, PassLookup passes, boolean speed)
            throws ParkException {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException(getType() + " attraction " + id + " does not accept this ticket");
        }
        return enter(accepted.cast(ticket), today, passes, speed);
    }

    public List<User> enterAny(AccessTicket ticket, LocalDate today, PassLookup passes) throws ParkException {
        return enterAny(ticket, today, passes, false).users();
    }

    protected boolean acceptsSpeedPasses() {
        return true;
    }

    /**
     * Passes consumed by this entry, without consuming them yet. With the SpeedPass option every
     * individual ticket needs one unconsumed SpeedPass (the first bought is used).
     */
    protected List<Pass> passesToConsume(T ticket, PassLookup passes, boolean speed) throws ParkException {
        List<Pass> selected = new ArrayList<>();
        if (speed) {
            for (IndividualTicket individual : ticket.individualTickets()) {
                selected.add(passes.passesOf(individual).stream()
                        .filter(p -> p instanceof SpeedPass && !p.isConsumed()).findFirst()
                        .orElseThrow(() -> new OperationNotAllowedException(
                                "Ticket " + individual.getId() + " has no unconsumed SpeedPass")));
            }
        }
        return selected;
    }
}
