package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A group of registered users, identified by a unique identifier. The group knows its members;
 * it never knows the tickets it bought.
 */
public class Group {

    private final String id;
    private String name;
    private final Set<User> members = new LinkedHashSet<>();

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

    public boolean hasMember(User user) {
        return members.contains(user);
    }

    /**
     * Adds a member; returns {@code false} if it already belonged to the group.
     */
    public boolean addMember(User user) {
        return members.add(user);
    }

    /**
     * Removes a member; returns {@code false} if it did not belong to the group.
     */
    public boolean removeMember(User user) {
        return members.remove(user);
    }

    /**
     * Current members, in order of addition.
     */
    public List<User> getMembers() {
        return new ArrayList<>(members);
    }
}
