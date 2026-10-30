package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named set of registered users. Groups do not know their tickets.
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

    public void addMember(User user) {
        if (contains(user)) {
            throw new DuplicateException("user " + user.getEmail() + " already belongs to group " + id);
        }
        members.add(user);
    }

    public void removeMember(User user) {
        if (!members.remove(user)) {
            throw new NotFoundException("user " + user.getEmail() + " does not belong to group " + id);
        }
    }
}
