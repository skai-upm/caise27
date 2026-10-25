package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are the registered User objects, not copies.
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
        if (hasMember(user)) {
            throw new ParkException("user " + user.getEmail() + " is already in group " + id);
        }
        members.add(user);
    }

    public void removeMember(User user) {
        if (!members.remove(user)) {
            throw new ParkException("user " + user.getEmail() + " is not in group " + id);
        }
    }
}
