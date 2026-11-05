package es.upm.parque.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.model.Group;
import es.upm.parque.model.User;

/**
 * Stores and retrieves groups, indexed by identifier and kept in creation order.
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

    public void remove(String id) {
        groupsById.remove(id);
    }

    public List<Group> findAll() {
        return new ArrayList<>(groupsById.values());
    }

    /** Removes the user from every group it belongs to. */
    public void removeUserEverywhere(User user) {
        groupsById.values().forEach(group -> group.remove(user));
    }
}
