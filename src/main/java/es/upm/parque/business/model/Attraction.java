package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside it. The type parameter bounds the kind
 * of access (ticket) the attraction accepts: {@link #enter(AttractionAccess)} only takes a {@code T}.
 *
 * @param <T> the access capability accepted by this attraction
 */
public abstract class Attraction<T extends AttractionAccess> {
    private final String id;
    private String name;
    private int capacity;
    private final Class<T> acceptedType;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> acceptedType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.acceptedType = acceptedType;
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

    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    /** Whether this attraction accepts the given kind of access. */
    public boolean accepts(AttractionAccess access) {
        return acceptedType.isInstance(access);
    }

    /**
     * Runtime entry point: lets the access in only if it is of the accepted kind.
     *
     * @param speed whether SpeedPass use is requested
     * @return the passes consumed by the entry
     */
    public List<Pass> admit(AttractionAccess access, boolean speed) {
        if (!accepts(access)) {
            throw new IllegalArgumentException(getType() + " attraction " + id + " does not accept ticket "
                    + access.getId());
        }
        return enter(acceptedType.cast(access), speed);
    }

    public List<Pass> admit(AttractionAccess access) {
        return admit(access, false);
    }

    public List<Pass> enter(T access) {
        return enter(access, false);
    }

    /**
     * Lets every user of the access in, or nobody. Passes and room are checked before the attraction changes, and
     * the selected passes are consumed only once the users are inside.
     *
     * @return the passes consumed by the entry
     */
    public List<Pass> enter(T access, boolean speed) {
        List<Pass> passes = passesToConsume(access, speed);
        List<User> users = access.getUsers();
        for (User u : users) {
            if (present.contains(u)) {
                throw new IllegalArgumentException(u.getDni() + " is already inside attraction " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new IllegalArgumentException("attraction " + id + " has " + getAvailable()
                    + " free places but ticket " + access.getId() + " needs " + users.size());
        }
        present.addAll(users);
        for (Pass pass : passes) {
            pass.consume();
        }
        return passes;
    }

    /**
     * Selects, without consuming them, the passes the entry needs. By default SPEED requires one unused SpeedPass
     * on every individual ticket of the access.
     */
    protected List<Pass> passesToConsume(T access, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualTicket ticket : access.getIndividualTickets()) {
            SpeedPass unused = null;
            for (SpeedPass pass : ticket.getSpeedPasses()) {
                if (!pass.isConsumed()) {
                    unused = pass;
                    break;
                }
            }
            if (unused == null) {
                throw new IllegalArgumentException("ticket " + ticket.getId() + " has no unused SpeedPass");
            }
            passes.add(unused);
        }
        return passes;
    }

    /** Removes the access's users that are inside and returns how many left. Any kind of access may leave. */
    public int exit(AttractionAccess access) {
        int before = present.size();
        present.removeAll(access.getUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalArgumentException("no user of ticket " + access.getId() + " is inside attraction " + id);
        }
        return left;
    }

    @Override
    public String toString() {
        return id + " (" + name + ", " + getType() + ", " + present.size() + "/" + capacity + ")";
    }
}
