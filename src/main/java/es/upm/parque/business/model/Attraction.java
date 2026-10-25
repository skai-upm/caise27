package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. The type parameter is the kind of
 * access the attraction admits; entry and exit only rely on the {@link AccessCapability} contract, so every
 * admitted ticket kind follows the same logic.
 *
 * @param <T> the access capability admitted by this attraction
 */
public abstract class Attraction<T extends AccessCapability> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> present = new ArrayList<>();

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

    public abstract AttractionType getType();

    public List<User> getPresent() {
        return List.copyOf(present);
    }

    public int getAvailablePlaces() {
        return capacity - present.size();
    }

    public boolean admits(AccessCapability access) {
        return accessType.isInstance(access);
    }

    /**
     * Returns the access as the type this attraction admits.
     *
     * @throws IllegalStateException if this attraction does not admit that kind of ticket
     */
    public T admit(AccessCapability access) {
        if (!admits(access)) {
            throw new IllegalStateException(getType() + " attraction " + id + " does not admit ticket "
                    + access.getId());
        }
        return accessType.cast(access);
    }

    /**
     * Lets in every user of the access, consuming the passes the entry requires, or nobody (and no pass) if any
     * condition fails.
     *
     * @throws IllegalStateException with the reason when the entry is rejected
     */
    public EntryResult enter(T access, boolean speed) {
        List<Pass> passes = passesToConsume(access, speed);
        List<User> users = access.getUsers();
        if (users.isEmpty()) {
            throw new IllegalStateException("Ticket " + access.getId() + " has no users");
        }
        for (User user : users) {
            if (present.contains(user)) {
                throw new IllegalStateException(user.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailablePlaces()) {
            throw new IllegalStateException("Not enough capacity in " + id + ": " + getAvailablePlaces()
                    + " available, " + users.size() + " needed");
        }
        present.addAll(users);
        for (Pass pass : passes) {
            pass.consume();
        }
        return new EntryResult(users.size(), passes);
    }

    /**
     * Selects the passes an entry consumes. With {@code speed}, every individual access must provide one unused
     * SpeedPass.
     *
     * @throws IllegalStateException if a required pass is missing
     */
    protected List<Pass> passesToConsume(T access, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualAccess individual : access.getIndividualAccesses()) {
            SpeedPass pass = individual.findUnusedSpeedPass();
            if (pass == null) {
                throw new IllegalStateException("Ticket " + individual.getId() + " has no unused SpeedPass");
            }
            passes.add(pass);
        }
        return passes;
    }

    /**
     * Lets out the users of the access that are inside. The ticket date is not checked.
     *
     * @return the number of users that left
     * @throws IllegalStateException if none of its users is inside
     */
    public int exit(AccessCapability access) {
        int before = present.size();
        present.removeAll(access.getUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalStateException("No user of ticket " + access.getId() + " is in attraction " + id);
        }
        return left;
    }
}
