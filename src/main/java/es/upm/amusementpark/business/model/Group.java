package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Groups do not know their tickets.
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

    public boolean hasMember(User user) {
        return members.contains(user);
    }

    public void addMember(User user) {
        members.add(user);
    }

    public boolean removeMember(User user) {
        return members.remove(user);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}
