package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of registered users. Groups do not know which tickets they purchased;
 * their locked state is derived by the park controller from the existing tickets.
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
