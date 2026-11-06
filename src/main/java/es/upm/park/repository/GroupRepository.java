package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.model.Group;

/**
 * Keeps the groups known to the application.
 */
public class GroupRepository {

    private final List<Group> groups = new ArrayList<>();

    public void add(Group group) {
        groups.add(group);
    }

    public void remove(Group group) {
        groups.remove(group);
    }

    public Optional<Group> findById(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public List<Group> findAll() {
        return Collections.unmodifiableList(groups);
    }
}
