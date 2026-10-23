package es.upm.park.business.model.attraction;

import es.upm.park.business.exception.AccessDeniedException;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.access.GeneralAccess;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.PassRegistry;
import es.upm.park.business.model.pass.SpeedPass;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction that admits tickets with access capability {@code T}.
 * It knows the users currently inside it; users do not know which
 * attraction they are in.
 *
 * @param <T> ticket access capability this attraction can handle
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

    public Class<T> getAccessType() {
        return accessType;
    }

    public abstract AttractionType getType();

    public List<User> getOccupants() {
        return List.copyOf(occupants);
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

    /**
     * Whether a ticket has the access capability this attraction handles.
     */
    public boolean accepts(GeneralAccess ticket) {
        return accessType.isInstance(ticket);
    }

    /**
     * Admits every holder of the ticket and consumes the passes the entry
     * requires, or changes nothing at all.
     *
     * @param speed whether the entry uses SpeedPasses
     */
    public EntryResult enter(T ticket, boolean speed, PassRegistry passes) throws AccessDeniedException {
        List<User> holders = ticket.getHolders();
        if (holders.size() > getAvailablePlaces()) {
            throw new AccessDeniedException("Not enough places in " + id + ": " + holders.size()
                    + " needed, " + getAvailablePlaces() + " available");
        }
        List<Pass> consumed = passesToConsume(ticket, speed, passes);
        for (Pass pass : consumed) {
            pass.consume();
        }
        occupants.addAll(holders);
        return new EntryResult(holders, consumed);
    }

    /**
     * Checks the pass conditions of this attraction type and returns the
     * passes an entry would consume. Must not change any state.
     */
    protected List<Pass> passesToConsume(T ticket, boolean speed, PassRegistry passes)
            throws AccessDeniedException {
        if (!speed) {
            return List.of();
        }
        return speedPassesFor(ticket.getAdmittedTickets(), passes);
    }

    /**
     * One unconsumed SpeedPass per entering individual ticket.
     */
    protected final List<Pass> speedPassesFor(List<IndividualTicket> tickets, PassRegistry passes)
            throws AccessDeniedException {
        List<Pass> selected = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            SpeedPass pass = passes.firstUnconsumedSpeedPass(ticket.getId()).orElseThrow(
                    () -> new AccessDeniedException("Ticket " + ticket.getId() + " has no unconsumed SpeedPass"));
            selected.add(pass);
        }
        return selected;
    }

    public boolean exit(User user) {
        return occupants.remove(user);
    }
}
