package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A group of registered users. Members are the users themselves, not copies.
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
        return Collections.unmodifiableList(members);
    }

    public boolean contains(User user) {
        return members.contains(user);
    }

    /**
     * Adds the user unless it is already a member; returns whether it was added.
     */
    public boolean addMember(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
