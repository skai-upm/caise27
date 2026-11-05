package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A group of registered users, identified by a unique identifier. A user appears at most once.
 */
public class Group {

    private final String id;
    private final String name;
    private final List<User> members = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Members in the order they were added; read-only view. */
    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public boolean contains(User user) {
        return members.stream().anyMatch(m -> m.getDni().equals(user.getDni()));
    }

    /** Adds the user unless already a member; returns whether it was added. */
    public boolean add(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Removes the user if a member; returns whether it was removed. */
    public boolean remove(User user) {
        return members.removeIf(m -> m.getDni().equals(user.getDni()));
    }
}
