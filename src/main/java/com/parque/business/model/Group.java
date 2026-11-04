package com.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A group of registered users. Members are references to the existing {@link User} objects, never copies,
 * and a user appears at most once. The group knows nothing about how it is stored.
 */
public class Group {
    private final String id;
    private String name;
    private final List<User> members = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Copy with the same identifier, name and member references (used to save a tentative state). */
    public Group(Group other) {
        this(other.id, other.name);
        members.addAll(other.members);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Members in insertion order (read-only view). */
    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** True if this exact user object is a member. */
    public boolean contains(User user) {
        for (User member : members) {
            if (member == user) {
                return true;
            }
        }
        return false;
    }

    /** Adds the user unless already a member; returns whether it was added. */
    public boolean addMember(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Removes the user if it is a member; returns whether it was removed. */
    public boolean removeMember(User user) {
        return members.removeIf(member -> member == user);
    }
}
