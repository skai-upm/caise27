package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * The type parameter is the access capability the attraction admits: entering
 * with any other capability does not compile, and {@link #admit} checks the
 * same bound for accesses resolved at run time from identifiers.
 *
 * @param <T> the access capability admitted by this attraction
 */
public abstract class Attraction<T extends AttractionAccess> {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    /** The access capability class admitted, i.e. the generic bound {@code T}. */
    protected abstract Class<T> accessType();

    public abstract AttractionType getType();

    /**
     * Entry for an access whose capability is only known at run time: it is
     * admitted only if it has the capability {@code T}.
     */
    public void admit(AttractionAccess access, LocalDate today) throws UserException {
        checkAdmission(access, today);
        present.addAll(access.getVisitors());
    }

    /**
     * Checks every entry condition (capability, date, visitors, presence, capacity)
     * without changing the attraction.
     */
    public void checkAdmission(AttractionAccess access, LocalDate today) throws UserException {
        if (!accessType().isInstance(access)) {
            String kind = access instanceof GroupAccess ? "group" : "individual";
            throw new UserException("Attraction " + id + " (" + getType() + ") does not admit " + kind + " tickets");
        }
        validate(access, today);
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

    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    public boolean isPresent(User user) {
        return present.stream().anyMatch(u -> u == user);
    }

    /**
     * Admits all users of the ticket, or none: the ticket must be for {@code today},
     * none of its users may already be inside, and all must fit.
     */
    public void enter(T ticket, LocalDate today) throws UserException {
        validate(ticket, today);
        present.addAll(ticket.getVisitors());
    }

    private void validate(AttractionAccess ticket, LocalDate today) throws UserException {
        if (!ticket.getDate().equals(today)) {
            throw new UserException("Ticket " + ticket.getId() + " is for " + ticket.getDate()
                    + ", not for " + today);
        }
        List<User> visitors = ticket.getVisitors();
        if (visitors.isEmpty()) {
            throw new UserException("Ticket " + ticket.getId() + " has no existing users");
        }
        for (User v : visitors) {
            if (isPresent(v)) {
                throw new UserException("User " + v.getEmail() + " is already in attraction " + id);
            }
        }
        if (visitors.size() > getAvailable()) {
            throw new UserException("Attraction " + id + " has " + getAvailable() + " free places; ticket "
                    + ticket.getId() + " needs " + visitors.size());
        }
    }

    /** Removes the ticket's users that are inside; fails if none of them is. */
    public void exit(AttractionAccess ticket) throws UserException {
        List<User> leaving = new ArrayList<>();
        for (User v : ticket.getVisitors()) {
            if (isPresent(v)) {
                leaving.add(v);
            }
        }
        if (leaving.isEmpty()) {
            throw new UserException("Nobody with ticket " + ticket.getId() + " is in attraction " + id);
        }
        present.removeIf(u -> leaving.stream().anyMatch(l -> l == u));
    }

    /** Removes a (deleted) user if present. */
    public void remove(User user) {
        present.removeIf(u -> u == user);
    }
}
