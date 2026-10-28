package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.upm.amusementpark.model.Group;

/**
 * Stores and retrieves groups by identifier, in creation order.
 */
public class GroupRepository {

    private final Map<String, Group> groups = new LinkedHashMap<>();

    public void save(Group group) {
        groups.put(group.getId(), group);
    }

    public Optional<Group> findById(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public void delete(String id) {
        groups.remove(id);
    }

    public List<Group> findAll() {
        return new ArrayList<>(groups.values());
    }
}
