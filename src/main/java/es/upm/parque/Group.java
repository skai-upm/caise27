package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are the registered {@link User} objects themselves.
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

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public boolean isMember(User user) {
        return members.contains(user);
    }

    /** @return false if the user was already a member */
    public boolean addMember(User user) {
        if (isMember(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** @return false if the user was not a member */
    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
