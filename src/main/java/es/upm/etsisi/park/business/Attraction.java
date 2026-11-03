package es.upm.etsisi.park.business;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction that admits tickets with the access capability {@code T}.
 * It knows the users currently inside it; users do not know the attraction.
 * Occupancy is runtime state only.
 *
 * @param <T> access capability a ticket needs to enter
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

    public Class<T> getAccessType() {
        return accessType;
    }

    public List<User> getOccupants() {
        return List.copyOf(occupants);
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * Narrows a ticket to the access capability this attraction handles.
     */
    public T accept(Ticket ticket) throws OperationNotAllowedException {
        if (!accessType.isInstance(ticket)) {
            String kind = ticket instanceof GroupTicket ? "group tickets" : "individual tickets";
            throw new OperationNotAllowedException(getType() + " attraction " + id + " does not accept " + kind);
        }
        return accessType.cast(ticket);
    }

    /**
     * Lets the ticket's users in, or nobody. Every check is made before any state
     * changes; on success the passes used are marked consumed and returned.
     */
    public List<Pass> enter(Ticket ticket, boolean speed, LocalDate today, PassLedger passes) throws ParkException {
        T access = accept(ticket);
        if (!ticket.isValidOn(today)) {
            throw new OperationNotAllowedException("ticket " + ticket.getId() + " is valid on " + ticket.getDate()
                    + ", not on the current system date " + today);
        }
        List<User> users = ticket.getHolders();
        if (users.size() > getAvailablePlaces()) {
            throw new OperationNotAllowedException("not enough places in attraction " + id + ": "
                    + users.size() + " needed, " + getAvailablePlaces() + " available");
        }
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new OperationNotAllowedException("user " + user.getEmail() + " is already inside attraction " + id);
            }
        }
        List<Pass> used = passesToConsume(access, speed, passes);
        occupants.addAll(users);
        used.forEach(Pass::markConsumed);
        return used;
    }

    /**
     * Passes this entry consumes. By default a SPEED entry needs one unused
     * SpeedPass for every individual ticket that enters.
     */
    protected List<Pass> passesToConsume(T access, boolean speed, PassLedger passes) throws ParkException {
        List<Pass> result = new ArrayList<>();
        if (!speed) {
            return result;
        }
        for (IndividualTicket ticket : access.entryTickets()) {
            SpeedPass pass = passes.unusedSpeedPass(ticket);
            if (pass == null) {
                throw new OperationNotAllowedException("ticket " + ticket.getId() + " has no unused SpeedPass");
            }
            result.add(pass);
        }
        return result;
    }

    void exit(User user) {
        occupants.remove(user);
    }

    void restore(List<User> previous) {
        occupants.clear();
        occupants.addAll(previous);
    }

    User findOccupant(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}
