package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Members are references to the registered {@link User} objects.
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

    public boolean addMember(User user) {
        if (user == null || members.contains(user)) {
            return false;
        }
        return members.add(user);
    }

    public boolean removeMember(User user) {
        return members.remove(user);
    }
}
