package es.upm.etsisi.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are references to the registered {@link User}
 * objects, never copies. Holds state only; it knows nothing about persistence.
 */
public class Group {

    private final String id;
    private String name;
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

    public void setName(String name) {
        this.name = name;
    }

    /** Unmodifiable view of the members, in the order they were added. */
    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** True if this exact user object belongs to the group. */
    public boolean contains(User user) {
        return members.stream().anyMatch(m -> m == user);
    }

    /** Adds the user if not already a member; returns whether it was added. */
    public boolean addMember(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Removes the user; returns whether it was a member. */
    public boolean removeMember(User user) {
        return members.removeIf(m -> m == user);
    }
}
