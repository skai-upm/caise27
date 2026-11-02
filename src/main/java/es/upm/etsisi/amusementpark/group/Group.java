package es.upm.etsisi.amusementpark.group;

import es.upm.etsisi.amusementpark.user.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. Groups never reference tickets; whether a group is
 * locked is derived from the tickets managed by the park.
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

    public boolean hasMember(User user) {
        return members.contains(user);
    }

    public void addMember(User user) {
        members.add(user);
    }

    /** Inserts a member at a position; used to restore a previous composition. */
    public void insertMember(int index, User user) {
        members.add(index, user);
    }

    /**
     * Removes a member.
     *
     * @return the position the member had, or -1 when the user was not a member
     */
    public int removeMember(User user) {
        int index = members.indexOf(user);
        if (index >= 0) {
            members.remove(index);
        }
        return index;
    }
}
