package es.upm.etsisi.park.business;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of registered users. Groups do not know their tickets; whether a
 * group is locked is derived from the park's tickets.
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

    /** Adds a member; used when building or loading a group. */
    public void addMember(User user) {
        members.add(user);
    }

    void add(int index, User user) {
        members.add(index, user);
    }

    int indexOf(User user) {
        return members.indexOf(user);
    }

    void remove(User user) {
        members.remove(user);
    }
}
