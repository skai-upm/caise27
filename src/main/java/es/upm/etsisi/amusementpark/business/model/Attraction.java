package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction that knows which users are currently inside it. The type parameter is the
 * ticket access capability the attraction accepts; only such tickets can be passed to {@link #enter}.
 *
 * @param <T> accepted access capability
 */
public abstract class Attraction<T extends TicketAccess> {

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
        return List.copyOf(occupants);
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    public boolean accepts(TicketAccess ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Narrows a ticket to the access capability of this attraction.
     */
    public T asAccepted(TicketAccess ticket) {
        if (!accepts(ticket)) {
            throw new OperationNotAllowedException("Attraction " + id + " (" + getType()
                    + ") does not accept ticket " + ticket.getId());
        }
        return accessType.cast(ticket);
    }

    /**
     * Lets every holder of the ticket in and consumes the passes the entry needs, or changes nothing.
     */
    public EntryResult enter(T ticket, boolean speed, PassLookup passes) {
        List<Pass> toConsume = passesToConsume(ticket, speed, passes);
        List<User> holders = ticket.getHolders();
        checkRoomFor(holders);
        occupants.addAll(holders);
        toConsume.forEach(Pass::consume);
        return new EntryResult(holders, toConsume);
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }

    /**
     * Passes consumed by an entry. By default one unconsumed SpeedPass per individual ticket when
     * SPEED is requested, none otherwise.
     */
    protected List<Pass> passesToConsume(T ticket, boolean speed, PassLookup passes) {
        List<Pass> result = new ArrayList<>();
        if (speed) {
            for (IndividualTicket t : ticket.getIndividualTickets()) {
                result.add(passes.findAvailableSpeedPass(t).orElseThrow(() ->
                        new OperationNotAllowedException("Ticket " + t.getId() + " has no unconsumed SpeedPass")));
            }
        }
        return result;
    }

    private void checkRoomFor(List<User> users) {
        if (users.size() > getAvailablePlaces()) {
            throw new OperationNotAllowedException("Not enough capacity: " + users.size()
                    + " user(s) need to enter, " + getAvailablePlaces() + " place(s) available");
        }
        for (User user : users) {
            if (isInside(user)) {
                throw new OperationNotAllowedException("User already inside: " + user.getEmail());
            }
        }
    }
}
