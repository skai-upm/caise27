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
     * Lets every user of the ticket enter, or none: the ticket must be valid today, the users not
     * already inside, the type-specific conditions met and the capacity sufficient.
     *
     * @return the users who entered
     */
    public final List<User> enter(T ticket, LocalDate today, PassLookup passes) throws ParkException {
        if (!ticket.getDate().equals(today)) {
            throw new OperationNotAllowedException("Ticket is not valid for the current date");
        }
        checkAccess(ticket, passes);
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
        return entering;
    }

    /**
     * Entry for callers holding any ticket: rejected unless this attraction accepts its kind.
     */
    public List<User> enterAny(AccessTicket ticket, LocalDate today, PassLookup passes) throws ParkException {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException(getType() + " attraction " + id + " does not accept this ticket");
        }
        return enter(accepted.cast(ticket), today, passes);
    }

    /**
     * Additional conditions of the attraction type; none by default.
     */
    protected void checkAccess(T ticket, PassLookup passes) throws ParkException {
        // no additional conditions
    }
}
