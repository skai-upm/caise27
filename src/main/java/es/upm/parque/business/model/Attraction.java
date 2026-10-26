package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. The type parameter is
 * the access capability the attraction admits: only tickets implementing it can enter, and they are
 * handled as that capability.
 *
 * @param <T> access capability admitted by this attraction
 */
public abstract class Attraction<T extends Admission> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> visitors = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
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

    public List<User> getVisitors() {
        return Collections.unmodifiableList(visitors);
    }

    public int getOccupancy() {
        return visitors.size();
    }

    public int getAvailablePlaces() {
        return capacity - visitors.size();
    }

    /**
     * True if tickets of this kind may enter the attraction.
     */
    public boolean accepts(Admission ticket) {
        return admitted.isInstance(ticket);
    }

    /**
     * Normal entry: lets in every user covered by the ticket without using SpeedPasses.
     */
    public EntryResult admit(Admission ticket) {
        return admit(ticket, false);
    }

    /**
     * Lets in every user covered by the ticket, using one SpeedPass per individual ticket when
     * {@code speed} is requested. Nobody enters and no pass is consumed unless the ticket kind is
     * admitted, the required passes are available, no user is already inside and all of them fit.
     */
    public EntryResult admit(Admission ticket, boolean speed) {
        if (!accepts(ticket)) {
            throw new ParkException("Attraction " + id + " (" + getType() + ") does not admit "
                    + (ticket instanceof GroupAdmission ? "group" : "individual") + " tickets");
        }
        if (speed && !allowsSpeed()) {
            throw new ParkException("SPEED is not accepted at attraction " + id + " (" + getType() + ")");
        }
        T typed = admitted.cast(ticket);
        List<Pass> passes = passesFor(typed, speed);
        List<User> users = usersOf(typed);
        if (users.isEmpty()) {
            throw new ParkException("Ticket " + ticket.getId() + " covers no users");
        }
        for (User u : users) {
            if (isPresent(u)) {
                throw new ParkException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new ParkException("Not enough places in attraction " + id + ": " + users.size()
                    + " requested, " + getAvailablePlaces() + " available");
        }
        visitors.addAll(users);
        passes.forEach(Pass::consume);
        return new EntryResult(users.size(), passes);
    }

    /**
     * Lets out the users of the ticket who are inside. Returns how many left.
     */
    public int release(Admission ticket) {
        int before = visitors.size();
        for (User u : ticket.getUsers()) {
            visitors.removeIf(v -> v == u);
        }
        return before - visitors.size();
    }

    /**
     * Whether entries may request SpeedPass use at this attraction.
     */
    protected boolean allowsSpeed() {
        return true;
    }

    /**
     * Passes the entry will consume, checked before anyone enters: with {@code speed}, one unused
     * SpeedPass for each individual ticket of the admission (all of them must have one); otherwise none.
     */
    protected List<Pass> passesFor(T ticket, boolean speed) {
        if (!speed) {
            return List.of();
        }
        List<Pass> passes = new ArrayList<>();
        for (IndividualTicket t : ticket.getIndividualTickets()) {
            SpeedPass pass = t.nextUnusedSpeedPass();
            if (pass == null) {
                throw new ParkException("Ticket " + t.getId() + " has no unused SpeedPass");
            }
            passes.add(pass);
        }
        return passes;
    }

    /**
     * Users admitted with the ticket, read through the admitted capability.
     */
    protected List<User> usersOf(T ticket) {
        return ticket.getUsers();
    }

    private boolean isPresent(User user) {
        return visitors.stream().anyMatch(v -> v == user);
    }
}
