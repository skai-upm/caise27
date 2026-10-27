package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.model.Group;

/**
 * Stores and retrieves user groups, indexed by identifier.
 */
public class GroupRepository {

    private final Map<String, Group> groupsById = new LinkedHashMap<>();

    public void add(Group group) {
        groupsById.put(group.getId(), group);
    }

    public Group findById(String id) {
        return groupsById.get(id);
    }

    public boolean exists(String id) {
        return groupsById.containsKey(id);
    }

    public void remove(Group group) {
        groupsById.remove(group.getId());
    }

    public List<Group> findAll() {
        return new ArrayList<>(groupsById.values());
    }
}
