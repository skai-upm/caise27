package es.upm.amusementpark.business;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Group of registered users, identified by a unique identifier. Members are referenced by e-mail.
 */
public class Group {

    private final String id;
    private final String name;
    private final Set<String> members = new LinkedHashSet<>();

    public Group(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** @return unmodifiable view of member e-mails in insertion order */
    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean hasMember(String email) {
        return members.contains(email);
    }

    /** @return {@code false} if the user was already a member */
    boolean addMember(String email) {
        return members.add(email);
    }

    /** Replaces a member's e-mail, keeping the membership. */
    void replaceMember(String oldEmail, String newEmail) {
        if (members.remove(oldEmail)) {
            members.add(newEmail);
        }
    }

    /** @return {@code false} if the user was not a member */
    boolean removeMember(String email) {
        return members.remove(email);
    }
}
