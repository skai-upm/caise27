package com.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are the park's own user objects.
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

    /**
     * Adds the user unless already a member; returns whether it was added.
     */
    public boolean add(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    public boolean remove(User user) {
        return members.remove(user);
    }
}
