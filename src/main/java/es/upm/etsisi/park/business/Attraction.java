package es.upm.etsisi.park.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An attraction that admits holders of tickets with access capability {@code T}.
 * Occupants are runtime state only and are never persisted.
 *
 * @param <T> access capability the attraction accepts
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final List<User> occupants = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accepted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getAvailable() {
        return capacity - occupants.size();
    }

    /** Whether entry may be requested with SpeedPasses. */
    public boolean acceptsSpeedPass() {
        return true;
    }

    /** Whether each entering ticket must hold an unconsumed Special Pass. */
    public boolean requiresSpecialPass() {
        return false;
    }

    /** Narrows a presented ticket to the capability this attraction handles, or rejects it. */
    T cast(AccessTicket ticket) {
        if (!accepted.isInstance(ticket)) {
            throw new RuleViolationException(getType() + " attraction " + id + " does not accept ticket " + ticket.getId());
        }
        return accepted.cast(ticket);
    }

    /** Checks that every holder of the ticket fits and none is already inside, without admitting anyone. */
    List<User> checkAdmission(T ticket) {
        List<User> users = ticket.getEntryTickets().stream().map(IndividualTicket::getUser).toList();
        if (users.size() > getAvailable()) {
            throw new RuleViolationException("Not enough capacity: " + users.size() + " needed, " + getAvailable() + " available");
        }
        for (User u : users) {
            if (occupants.contains(u)) {
                throw new ConflictException("User already inside: " + u.getEmail());
            }
        }
        return users;
    }

    /** Admits all holders of the ticket together or none of them. */
    void admit(T ticket) {
        occupants.addAll(checkAdmission(ticket));
    }

    void removeOccupants(List<User> users) {
        occupants.removeAll(users);
    }

    void exit(String email) {
        for (int i = 0; i < occupants.size(); i++) {
            if (occupants.get(i).getEmail().equals(email)) {
                occupants.remove(i);
                return;
            }
        }
        throw new NotFoundException("User not inside attraction: " + email);
    }
}
