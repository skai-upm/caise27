package es.upm.etsisi.park.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named set of users. Groups know their members but not the tickets they bought.
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

    public boolean contains(User user) {
        return members.contains(user);
    }

    public void add(User user) {
        members.add(user);
    }

    public void remove(User user) {
        members.remove(user);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}
