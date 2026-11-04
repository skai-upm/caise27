package es.upm.park;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are references to the existing
 * {@link User} objects, never copies.
 */
public class Group {

    private final String id;
    private final String name;
    private final List<User> members;

    public Group(String id, String name) {
        this(id, name, new ArrayList<>());
    }

    private Group(String id, String name, List<User> members) {
        this.id = id;
        this.name = name;
        this.members = members;
    }

    /** Returns a group with the same id and name and its own copy of the member list. */
    Group copy() {
        return new Group(id, name, new ArrayList<>(members));
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

    public boolean contains(User user) {
        return members.contains(user);
    }

    void add(User user) {
        members.add(user);
    }

    boolean remove(User user) {
        return members.remove(user);
    }
}
