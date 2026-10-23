package es.upm.etsisi.park.business.model.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.park.business.model.user.User;

/**
 * A named set of registered users. Groups know their members but hold no
 * references to the tickets they buy; whether a group is locked is derived
 * by the park from its tickets.
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

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean contains(User user) {
        return members.contains(user);
    }

    /** Adds the user unless already a member; returns whether it was added. */
    public boolean addMember(User user) {
        if (members.contains(user)) {
            return false;
        }
        return members.add(user);
    }

    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
