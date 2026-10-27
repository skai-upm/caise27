package es.upm.amusementpark.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. A user appears at most once in a group.
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

    public boolean contains(User user) {
        return members.contains(user);
    }

    /** Adds the user; returns false if it was already a member. */
    public boolean addMember(User user) {
        if (members.contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Removes the user; returns false if it was not a member. */
    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
