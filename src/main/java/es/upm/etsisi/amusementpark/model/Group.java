package es.upm.etsisi.amusementpark.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of existing users. A user appears at most once in a group.
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

    public boolean hasMember(String email) {
        return members.stream().anyMatch(user -> user.getEmail().equals(email));
    }

    /**
     * @throws IllegalArgumentException if the user already belongs to the group
     */
    public void addMember(User user) {
        if (hasMember(user.getEmail())) {
            throw new IllegalArgumentException("User " + user.getEmail() + " already belongs to group " + identifier);
        }
        members.add(user);
    }

    /**
     * @throws IllegalArgumentException if the user does not belong to the group
     */
    public void removeMember(String email) {
        if (!members.removeIf(user -> user.getEmail().equals(email))) {
            throw new IllegalArgumentException("User " + email + " does not belong to group " + identifier);
        }
    }
}
