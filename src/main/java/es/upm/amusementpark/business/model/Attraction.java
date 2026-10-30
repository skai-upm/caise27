package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction that accepts tickets with access capability {@code T}. It knows the users inside it; users do
 * not know the attraction.
 *
 * @param <T> the access capability a ticket must have to enter
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean accepts(AccessTicket ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Narrows a ticket to the access capability this attraction handles, or rejects it.
     */
    public T admit(AccessTicket ticket) {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException("attraction " + id + " (" + getType()
                    + ") does not accept ticket " + ticket.getId());
        }
        return accessType.cast(ticket);
    }

    /**
     * Lets every holder of the ticket in and consumes the required passes, all or nothing.
     *
     * @return the passes consumed by this entry
     */
    public List<Pass> enter(T ticket, boolean speed, PassWallet wallet) {
        List<User> users = ticket.getHolders();
        for (User user : users) {
            if (occupants.contains(user)) {
                throw new OperationNotAllowedException("user " + user.getEmail() + " is already inside attraction "
                        + id);
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new OperationNotAllowedException("not enough capacity in attraction " + id + ": " + users.size()
                    + " user(s) need to enter and " + getAvailablePlaces() + " place(s) are available");
        }
        List<Pass> passes = passesToConsume(ticket, speed, wallet);
        occupants.addAll(users);
        for (Pass pass : passes) {
            pass.consume();
        }
        return passes;
    }

    /**
     * Passes this entry must consume. By default, one unconsumed SpeedPass per admitted ticket when SPEED is used.
     * Must not change any state.
     */
    protected List<Pass> passesToConsume(T ticket, boolean speed, PassWallet wallet) {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualTicket admitted : ticket.getAdmittedTickets()) {
            SpeedPass pass = wallet.availableSpeedPass(admitted);
            if (pass == null) {
                throw new OperationNotAllowedException("ticket " + admitted.getId()
                        + " has no unconsumed SpeedPass");
            }
            passes.add(pass);
        }
        return passes;
    }

    public void exit(User user) {
        if (!occupants.remove(user)) {
            throw new NotFoundException("user " + user.getEmail() + " is not inside attraction " + id);
        }
    }
}
