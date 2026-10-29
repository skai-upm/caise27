package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.exception.ForbiddenOperationException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.model.GeneralAccess;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.PassLookup;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction that admits tickets with access capability {@code T}. It knows the users inside
 * it; users do not know the attraction.
 *
 * @param <T> access capability a ticket must have to enter
 */
public abstract class Attraction<T extends GeneralAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
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

    public List<User> getOccupants() {
        return List.copyOf(occupants);
    }

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public abstract AttractionType getType();

    /**
     * Ticket capability accepted by this attraction.
     */
    protected abstract Class<T> accessType();

    public boolean accepts(Ticket ticket) {
        return accessType().isInstance(ticket);
    }

    /**
     * Admits the users of the ticket. All checks happen before any state change, so either every
     * user enters and every required pass is consumed, or nothing changes.
     */
    public final EntryResult enter(Ticket ticket, boolean speed, LocalDate today, PassLookup passes)
            throws ParkException {
        if (!accepts(ticket)) {
            throw new ForbiddenOperationException(getType() + " attraction " + id + " does not accept ticket "
                    + ticket.getId());
        }
        return admit(accessType().cast(ticket), speed, today, passes);
    }

    private EntryResult admit(T access, boolean speed, LocalDate today, PassLookup passes) throws ParkException {
        if (!access.isValidOn(today)) {
            throw new ForbiddenOperationException("ticket " + access.getId() + " is not valid on " + today);
        }
        List<Pass> toConsume = requiredPasses(access, speed, passes);
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : access.entryTickets()) {
            User user = ticket.getUser();
            if (occupants.contains(user)) {
                throw new ForbiddenOperationException("user already inside the attraction: " + user.getEmail());
            }
            users.add(user);
        }
        if (users.size() > getAvailablePlaces()) {
            throw new ForbiddenOperationException("not enough capacity: " + users.size() + " needed, "
                    + getAvailablePlaces() + " available");
        }
        for (Pass pass : toConsume) {
            pass.consume();
        }
        occupants.addAll(users);
        return new EntryResult(users.size(), toConsume);
    }

    /**
     * Passes that must be consumed for this entry. By default none, or one unconsumed SpeedPass per
     * entering ticket when {@code speed} is requested.
     */
    protected List<Pass> requiredPasses(T access, boolean speed, PassLookup passes) throws ParkException {
        List<Pass> required = new ArrayList<>();
        if (!speed) {
            return required;
        }
        for (IndividualTicket ticket : access.entryTickets()) {
            SpeedPass pass = passes.firstUnconsumedSpeedPass(ticket);
            if (pass == null) {
                throw new ForbiddenOperationException("ticket " + ticket.getId() + " has no unconsumed SpeedPass");
            }
            required.add(pass);
        }
        return required;
    }

    public User findOccupant(String email) {
        for (User user : occupants) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public void exit(User user) {
        occupants.remove(user);
    }
}
