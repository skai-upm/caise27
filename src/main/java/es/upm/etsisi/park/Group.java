package es.upm.etsisi.park;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Groups hold no references to tickets.
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

    void add(User user) {
        members.add(user);
    }

    void add(int index, User user) {
        members.add(index, user);
    }

    /** Removes the user and returns the position it had, or -1 if it was not a member. */
    int remove(User user) {
        int index = members.indexOf(user);
        if (index >= 0) {
            members.remove(index);
        }
        return index;
    }

    User findMember(String email) {
        for (User u : members) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }
}
