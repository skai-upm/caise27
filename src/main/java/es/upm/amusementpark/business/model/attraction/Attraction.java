package es.upm.amusementpark.business.model.attraction;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.PassLookup;
import es.upm.amusementpark.business.model.ticket.AccessTicket;
import es.upm.amusementpark.business.model.ticket.IndividualAccess;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction that admits tickets of access type {@code T}. It knows the users currently inside
 * it; users do not know the attraction.
 *
 * @param <T> the ticket access capability this attraction accepts
 */
public abstract class Attraction<T extends AccessTicket> {

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

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }

    public boolean accepts(AccessTicket ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Admits the users of any ticket this attraction accepts.
     *
     * @throws OperationNotAllowedException if the ticket kind is not accepted or entry is not possible
     */
    public final EntryResult admit(AccessTicket ticket, boolean speed, PassLookup passes, LocalDate today)
            throws OperationNotAllowedException {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException(getType() + " attraction " + id
                    + " does not accept ticket " + ticket.getId());
        }
        return enter(accessType.cast(ticket), speed, passes, today);
    }

    /**
     * All-or-nothing entry: every check (validity, presence, capacity, passes) runs before any user is
     * admitted or any pass is consumed.
     */
    protected final EntryResult enter(T ticket, boolean speed, PassLookup passes, LocalDate today)
            throws OperationNotAllowedException {
        if (!ticket.isValidOn(today)) {
            throw new OperationNotAllowedException("ticket is not valid today: " + ticket.getId());
        }
        List<User> entering = ticket.getUsers();
        for (User user : entering) {
            if (occupants.contains(user)) {
                throw new OperationNotAllowedException("user already inside the attraction: " + user.getEmail());
            }
        }
        if (entering.size() > getFreePlaces()) {
            throw new OperationNotAllowedException("not enough capacity: " + getFreePlaces()
                    + " place(s) available, " + entering.size() + " needed");
        }
        List<Pass> toConsume = selectPasses(ticket, speed, passes);
        occupants.addAll(entering);
        for (Pass pass : toConsume) {
            pass.consume();
        }
        return new EntryResult(entering.size(), toConsume);
    }

    /**
     * Passes that a successful entry consumes. By default, SPEED requires one available SpeedPass per
     * admitted individual ticket.
     */
    protected List<Pass> selectPasses(T ticket, boolean speed, PassLookup passes)
            throws OperationNotAllowedException {
        return speed ? speedPassesFor(ticket.getAccessTickets(), passes) : List.of();
    }

    protected static List<Pass> speedPassesFor(List<? extends IndividualAccess> tickets, PassLookup passes)
            throws OperationNotAllowedException {
        List<Pass> selected = new ArrayList<>();
        for (IndividualAccess ticket : tickets) {
            selected.add(passes.availableSpeedPass(ticket).orElseThrow(() ->
                    new OperationNotAllowedException("no unconsumed SpeedPass for ticket " + ticket.getId())));
        }
        return selected;
    }
}
