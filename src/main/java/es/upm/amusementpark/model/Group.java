package es.upm.amusementpark.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A group of registered users, identified by a unique identifier. Members are referenced by their
 * DNI, which never changes.
 */
public class Group {

    private final String id;
    private String name;
    private final Set<String> memberDnis = new LinkedHashSet<>();

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

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasMember(String dni) {
        return memberDnis.contains(dni);
    }

    /**
     * Adds a member; returns {@code false} if it already belonged to the group.
     */
    public boolean addMember(String dni) {
        return memberDnis.add(dni);
    }

    /**
     * Removes a member; returns {@code false} if it did not belong to the group.
     */
    public boolean removeMember(String dni) {
        return memberDnis.remove(dni);
    }

    public List<String> getMemberDnis() {
        return new ArrayList<>(memberDnis);
    }
}
