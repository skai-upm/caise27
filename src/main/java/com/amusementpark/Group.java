package com.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are references to the existing user objects.
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

    void remove(User user) {
        members.remove(user);
    }
}
