package es.upm.parque;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of registered users. Members are the same {@link User} objects
 * known by the system, never copies.
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

    void addMember(User user) {
        members.add(user);
    }

    boolean removeMember(User user) {
        return members.remove(user);
    }
}
