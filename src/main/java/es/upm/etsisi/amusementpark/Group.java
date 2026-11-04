package es.upm.etsisi.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are the same {@link User}
 * objects managed by {@link UserManager}; they are compared by identity.
 */
public class Group {

    private final String id;
    private String name;
    private final List<User> members = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Copy with the same member references, used to restore a previous state. */
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

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public boolean contains(User user) {
        return members.stream().anyMatch(m -> m == user);
    }

    /** Adds the user unless already a member; returns whether it was added. */
    public boolean addMember(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Removes the user if a member; returns whether it was removed. */
    public boolean removeMember(User user) {
        return members.removeIf(m -> m == user);
    }
}
