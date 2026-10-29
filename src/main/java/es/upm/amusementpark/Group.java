package es.upm.amusementpark;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A group of existing users. A user appears at most once in a group.
 */
public class Group {

    private final String identifier;
    private final String name;
    private final Set<User> members = new LinkedHashSet<>();

    public Group(String identifier, String name) {
        this.identifier = Objects.requireNonNull(identifier);
        this.name = Objects.requireNonNull(name);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public Set<User> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /** @return {@code false} if the user already belongs to the group */
    public boolean addMember(User user) {
        return members.add(user);
    }

    /** @return {@code false} if the user did not belong to the group */
    public boolean removeMember(User user) {
        return members.remove(user);
    }

    public boolean hasMember(User user) {
        return members.contains(user);
    }
}
