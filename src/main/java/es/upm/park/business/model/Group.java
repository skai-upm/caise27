package es.upm.park.business.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A named group of registered users, identified by a unique identifier.
 * Members are referenced by email and appear at most once.
 */
public class Group {

    private final String id;
    private final String name;
    private final Set<String> memberEmails = new LinkedHashSet<>();

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

    public Set<String> getMemberEmails() {
        return Collections.unmodifiableSet(memberEmails);
    }

    public boolean hasMember(String email) {
        return memberEmails.contains(email);
    }

    /** @return true if the user was added, false if already a member */
    public boolean addMember(String email) {
        return memberEmails.add(email);
    }

    /** @return true if the user was removed, false if not a member */
    public boolean removeMember(String email) {
        return memberEmails.remove(email);
    }
}
