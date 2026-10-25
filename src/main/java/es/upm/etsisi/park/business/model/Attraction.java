package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.exception.AccessDeniedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A park attraction restricted to tickets with access capability {@code T}.
 * It knows which users are inside it; users do not know the attraction.
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

    public int getOccupancy() {
        return occupants.size();
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    public boolean accepts(AccessTicket ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Narrows a ticket to this attraction's access capability.
     *
     * @throws AccessDeniedException if the attraction does not accept this kind of ticket
     */
    public T asAccepted(AccessTicket ticket) {
        if (!accepts(ticket)) {
            throw new AccessDeniedException(getType() + " attraction " + id + " does not accept ticket "
                    + ticket.getId());
        }
        return accessType.cast(ticket);
    }

    /**
     * Lets every holder of the ticket in and consumes the required passes, or changes nothing.
     */
    public EntryResult enter(T ticket, LocalDate today, boolean speed) {
        if (!ticket.isValidOn(today)) {
            throw new AccessDeniedException("Ticket " + ticket.getId() + " is not valid on " + today);
        }
        List<User> holders = ticket.getHolders();
        for (User holder : holders) {
            if (occupants.contains(holder)) {
                throw new AccessDeniedException("User " + holder.getEmail() + " is already inside " + id);
            }
        }
        if (holders.size() > getAvailablePlaces()) {
            throw new AccessDeniedException("Not enough capacity in " + id + ": " + holders.size() + " needed, "
                    + getAvailablePlaces() + " available");
        }
        List<Pass> passes = passesToConsume(ticket, speed);
        occupants.addAll(holders);
        for (Pass pass : passes) {
            pass.consume();
        }
        return new EntryResult(holders, passes);
    }

    /**
     * Selects the passes this entry consumes. Must not change state; throws if a requirement is not met.
     * By default SPEED needs one unconsumed SpeedPass per entering individual ticket.
     */
    protected List<Pass> passesToConsume(T ticket, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualTicket individual : ticket.getAccessTickets()) {
            SpeedPass pass = individual.findAvailableSpeedPass()
                    .orElseThrow(() -> new AccessDeniedException("Ticket " + individual.getId()
                            + " has no unconsumed SpeedPass"));
            passes.add(pass);
        }
        return passes;
    }

    public Optional<User> findOccupant(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean release(User user) {
        return occupants.remove(user);
    }
}
