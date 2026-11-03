package es.upm.etsisi.amusementpark.business.group;

import es.upm.etsisi.amusementpark.business.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. The group holds no reference to tickets;
 * whether it is locked is derived from the tickets managed by the park.
 */
public class Group {

    private final String identifier;
    private final String name;
    private final List<User> members = new ArrayList<>();

    public Group(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
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

    public int indexOf(User user) {
        return members.indexOf(user);
    }

    public void addMember(User user) {
        members.add(user);
    }

    public void addMember(int index, User user) {
        members.add(index, user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }
}
