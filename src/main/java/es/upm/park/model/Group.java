package es.upm.park.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A named set of registered users. Groups do not know the tickets they
 * buy; whether a group is locked is derived from the park's tickets.
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
        return List.copyOf(members);
    }

    public int size() {
        return members.size();
    }

    public boolean contains(User user) {
        return members.contains(user);
    }

    /**
     * @return {@code false} if the user already belongs to the group
     */
    public boolean add(User user) {
        if (members.contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    public boolean remove(User user) {
        return members.remove(user);
    }
}
