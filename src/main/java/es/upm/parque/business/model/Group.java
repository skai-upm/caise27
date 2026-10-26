package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are the existing user objects, not copies.
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

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public boolean hasMember(User user) {
        return members.contains(user);
    }

    /**
     * Adds the user unless already present. Returns whether the user was added.
     */
    public boolean addMember(User user) {
        if (hasMember(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
