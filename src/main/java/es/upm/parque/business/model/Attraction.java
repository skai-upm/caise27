package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * The type parameter bounds which access credentials the attraction handles.
 *
 * @param <T> access capability admitted by this attraction
 */
public abstract class Attraction<T extends AccessCredential> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> present = new ArrayList<>();

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

    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    public abstract AttractionType getType();

    /**
     * Whether entries may request SpeedPass use.
     */
    public boolean allowsSpeed() {
        return true;
    }

    /**
     * Whether entries must consume a Special Pass.
     */
    public boolean requiresSpecialPass() {
        return false;
    }

    /**
     * Returns the credential typed for this attraction, or fails if this attraction does not admit it.
     */
    public T admit(AccessCredential credential) {
        if (!admitted.isInstance(credential)) {
            throw new ParkException(getType() + " attraction " + id + " does not admit ticket " + credential.getId());
        }
        return admitted.cast(credential);
    }

    /**
     * Users that a credential brings into the attraction.
     */
    protected List<User> usersOf(T credential) {
        return credential.getUsers();
    }

    /**
     * Admits every user of the credential, or none if any check fails.
     *
     * @return number of users that entered
     */
    public int enter(T credential) {
        List<User> users = usersOf(credential);
        for (User user : users) {
            if (present.contains(user)) {
                throw new ParkException("user " + user.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new ParkException("attraction " + id + " has " + getAvailable()
                    + " available places and ticket " + credential.getId() + " needs " + users.size());
        }
        present.addAll(users);
        return users.size();
    }

    /**
     * Removes the credential's users that are inside. Any kind of credential may leave.
     *
     * @return number of users that left
     */
    public int exit(AccessCredential credential) {
        int left = 0;
        for (User user : credential.getUsers()) {
            if (present.remove(user)) {
                left++;
            }
        }
        if (left == 0) {
            throw new ParkException("no user of ticket " + credential.getId() + " is in attraction " + id);
        }
        return left;
    }
}
