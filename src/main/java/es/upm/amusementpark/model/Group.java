package es.upm.amusementpark.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A group of registered users identified by a unique identifier.
 */
public class Group {

    private final String identifier;
    private String name;
    private final List<User> members = new ArrayList<>();

    public Group(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
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

    /** Adds a member; returns false when the user already belongs to the group. */
    public boolean add(User user) {
        if (contains(user)) {
            return false;
        }
        return members.add(user);
    }

    public boolean remove(User user) {
        return members.remove(user);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}
