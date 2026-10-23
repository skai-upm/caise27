package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of registered users. A group does not know which tickets it
 * has bought; that is derived from the tickets themselves.
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

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean contains(User user) {
        return members.contains(user);
    }

    /**
     * Adds {@code user} at the end of the group.
     *
     * @return {@code false} if the user was already a member
     */
    public boolean add(User user) {
        if (contains(user)) {
            return false;
        }
        members.add(user);
        return true;
    }

    /** Puts {@code user} back at {@code position}, used to undo a removal. */
    public void add(int position, User user) {
        if (!contains(user)) {
            members.add(Math.min(position, members.size()), user);
        }
    }

    /**
     * Removes {@code user} from the group.
     *
     * @return the position the user had, or {@code -1} if not a member
     */
    public int remove(User user) {
        int position = members.indexOf(user);
        if (position >= 0) {
            members.remove(position);
        }
        return position;
    }
}
