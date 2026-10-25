package es.upm.etsisi.park.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named set of users. Groups do not know the tickets they purchase.
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

    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * @return {@code false} if the user was already a member
     */
    public boolean add(User user) {
        if (members.contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /**
     * @return {@code false} if the user was not a member
     */
    public boolean remove(User user) {
        return members.remove(user);
    }
}
