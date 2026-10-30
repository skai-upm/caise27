package es.upm.etsisi.amusementpark.business.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A named set of users. Groups do not know their tickets.
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
     * Adds a user unless already a member.
     *
     * @return false if the user was already in the group
     */
    public boolean add(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    public boolean remove(User user) {
        return members.remove(user);
    }
}
