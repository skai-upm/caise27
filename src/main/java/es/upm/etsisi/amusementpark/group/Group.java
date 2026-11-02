package es.upm.etsisi.amusementpark.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.user.User;

/**
 * A named set of registered users. A group holds no reference to tickets; whether
 * it is locked is derived by the park from the existing group tickets.
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
        return members.stream().anyMatch(m -> m == user);
    }

    public void addMember(User user) {
        members.add(user);
    }

    public void addMember(int index, User user) {
        members.add(index, user);
    }

    /**
     * Removes the user and returns the position it had, or -1 if it was not a member.
     */
    public int removeMember(User user) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i) == user) {
                members.remove(i);
                return i;
            }
        }
        return -1;
    }
}
