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
     */
    public void admit(AttractionAccess access) {
        if (!accepts(access)) {
            throw new IllegalArgumentException(getType() + " attraction " + id + " does not accept ticket "
                    + access.getId());
        }
        enter(acceptedType.cast(access));
    }

    /**
     * Lets every user of the access in, or nobody: all checks are done before the attraction changes.
     */
    public void enter(T access) {
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
