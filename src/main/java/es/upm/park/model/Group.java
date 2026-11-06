package es.upm.park.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named group of existing users, identified by a unique identifier.
 * Members are referenced by their immutable DNI. Whether the group can be
 * modified is not stored here: it is derived from its tickets and the date.
 */
public class Group {

    private final String id;
    private final String name;
    private final List<String> memberDnis = new ArrayList<>();

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

    public boolean contains(String dni) {
        return memberDnis.contains(dni);
    }

    /** Adds a member; returns false if the DNI is already a member. */
    public boolean addMember(String dni) {
        if (contains(dni)) {
            return false;
        }
        return memberDnis.add(dni);
    }

    public boolean removeMember(String dni) {
        return memberDnis.remove(dni);
    }

    public List<String> getMemberDnis() {
        return Collections.unmodifiableList(memberDnis);
    }
}
