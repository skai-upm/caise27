package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;

/**
 * A park attraction that accepts tickets with access capability {@code T}. It knows the users
 * currently inside it; users do not know the attraction.
 *
 * @param <T> access capability a ticket must have to enter this attraction
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

    public Class<T> getAccessType() {
        return accessType;
    }

    public List<User> getOccupants() {
        return List.copyOf(occupants);
    }

    public int getAvailable() {
        return capacity - occupants.size();
    }

    /**
     * Admits the ticket's holders, consuming passes as required. Every condition is checked before
     * any state changes, so a failed entry leaves occupants and passes untouched.
     */
    public EntryResult enter(T access, LocalDate today, boolean speed) {
        if (!access.isValidOn(today)) {
            throw new OperationNotAllowedException("ticket " + access.getId() + " is not valid on " + today);
        }
        List<Pass> passes = selectPasses(access, speed);
        List<User> users = access.getHolders();
        if (users.size() > getAvailable()) {
            throw new OperationNotAllowedException("not enough capacity in attraction " + id
                    + " (" + getAvailable() + " available, " + users.size() + " needed)");
        }
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new OperationNotAllowedException("user already inside attraction " + id + ": " + user.getEmail());
            }
        }
        occupants.addAll(users);
        passes.forEach(Pass::consume);
        return new EntryResult(users, passes);
    }

    /**
     * Chooses the passes that a successful entry will consume. By default, SPEED entry needs one
     * unconsumed SpeedPass per entering individual ticket and normal entry consumes nothing.
     */
    protected List<Pass> selectPasses(T access, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualTicket ticket : access.getEntryTickets()) {
            passes.add(ticket.firstAvailableSpeedPass().orElseThrow(() -> new OperationNotAllowedException(
                    "ticket " + ticket.getId() + " has no unconsumed SpeedPass")));
        }
        return passes;
    }

    public void exit(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                occupants.remove(user);
                return;
            }
        }
        throw new NotFoundException("user not inside attraction " + id + ": " + email);
    }
}
