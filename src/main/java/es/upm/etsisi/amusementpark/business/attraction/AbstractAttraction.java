package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.ticket.AccessTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Attraction restricted to tickets with access capability {@code T}. Entry only
 * reaches {@link #admit} with a ticket of that capability.
 *
 * @param <T> access capability accepted by this attraction
 */
public abstract class AbstractAttraction<T extends AccessTicket> implements Attraction {

    private final String identifier;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> occupants = new ArrayList<>();

    protected AbstractAttraction(String identifier, String name, int capacity, Class<T> accessType) {
        this.identifier = identifier;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    @Override
    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    @Override
    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    @Override
    public final EntryResult enter(Ticket ticket, boolean speed, PassLookup passes) throws ParkException {
        if (!accessType.isInstance(ticket)) {
            throw new RuleViolationException(getType() + " attraction " + identifier + " does not accept ticket "
                    + ticket.getIdentifier());
        }
        return admit(accessType.cast(ticket), speed, passes);
    }

    /** Checks every condition first, then admits all holders and consumes the passes. */
    protected EntryResult admit(T ticket, boolean speed, PassLookup passes) throws ParkException {
        List<Pass> required = requiredPasses(ticket, speed, passes);
        List<User> holders = ticket.getHolders();
        for (User holder : holders) {
            if (isInside(holder)) {
                throw new RuleViolationException("user " + holder.getEmail() + " is already inside " + identifier);
            }
        }
        if (getAvailablePlaces() < holders.size()) {
            throw new RuleViolationException("not enough places in " + identifier + " for " + holders.size()
                    + " user(s)");
        }
        occupants.addAll(holders);
        required.forEach(Pass::consume);
        return new EntryResult(holders, required);
    }

    /** With SPEED, every entering ticket must provide one unconsumed SpeedPass. */
    protected List<Pass> requiredPasses(T ticket, boolean speed, PassLookup passes) throws ParkException {
        List<Pass> required = new ArrayList<>();
        if (speed) {
            for (IndividualTicket entering : ticket.getEntryTickets()) {
                required.add(passes.findUnusedSpeedPass(entering).orElseThrow(() -> new RuleViolationException(
                        "ticket " + entering.getIdentifier() + " has no unused SpeedPass")));
            }
        }
        return required;
    }

    @Override
    public void release(User user) {
        occupants.remove(user);
    }
}
