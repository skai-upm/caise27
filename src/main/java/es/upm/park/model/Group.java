package es.upm.park.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of existing users, identified by a unique identifier. The
 * group references its member users; whether it can be modified is not
 * stored here: it is derived from its tickets and the current date.
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

    /** Whether a user with the same DNI is a member. */
    public boolean contains(User user) {
        return members.stream().anyMatch(m -> m.getDni().equals(user.getDni()));
    }

    /** Adds a member; returns false if the user is already a member. */
    public boolean addMember(User user) {
        if (contains(user)) {
            return false;
        }
        return members.add(user);
    }

    public boolean removeMember(User user) {
        return members.removeIf(m -> m.getDni().equals(user.getDni()));
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** DNIs of the members, in membership order. */
    public List<String> getMemberDnis() {
        return members.stream().map(User::getDni).toList();
    }
}
