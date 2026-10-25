package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of registered users. Members are the same {@link User} objects managed by the rest of the system.
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

    public List<User> getMembers() {
        return List.copyOf(members);
    }

    public boolean hasMember(User user) {
        return members.contains(user);
    }

    /**
     * Adds the user unless it is already a member. Returns whether it was added.
     */
    public boolean addMember(User user) {
        if (hasMember(user)) {
            return false;
        }
        return members.add(user);
    }

    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
