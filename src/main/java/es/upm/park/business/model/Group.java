package es.upm.park.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are references to the registered {@link User}
 * objects, never copies.
 */
public class Group {

    private final String id;
    private final String name;
    private final List<User> members = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Copy with its own member list that refers to the same user objects. */
    public Group(Group other) {
        this(other.id, other.name);
        members.addAll(other.members);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** Whether this exact user object is a member. */
    public boolean contains(User user) {
        return members.stream().anyMatch(m -> m == user);
    }

    /** Adds the user unless already present; returns whether it was added. */
    public boolean addMember(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Removes the user if present; returns whether it was removed. */
    public boolean removeMember(User user) {
        return members.removeIf(m -> m == user);
    }
}
