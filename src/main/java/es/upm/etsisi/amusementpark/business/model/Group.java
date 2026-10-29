package es.upm.etsisi.amusementpark.business.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of registered users. Groups do not know their tickets; whether a group is locked
 * is derived from the tickets managed by the park.
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

    public boolean contains(User user) {
        return members.contains(user);
    }

    /**
     * Adds a member; returns false if the user is already in the group.
     */
    public boolean add(User user) {
        if (members.contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    public boolean remove(User user) {
        return members.remove(user);
    }
}
