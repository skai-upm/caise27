package es.upm.amusementpark.repository;

import es.upm.amusementpark.model.Group;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves groups by identifier.
 */
public class GroupRepository {

    private final Map<String, Group> groups = new LinkedHashMap<>();

    public void add(Group group) {
        groups.put(group.getIdentifier(), group);
    }

    public Optional<Group> findById(String identifier) {
        return Optional.ofNullable(groups.get(identifier));
    }

    public void remove(Group group) {
        groups.remove(group.getIdentifier());
    }

    public List<Group> findAll() {
        return Collections.unmodifiableList(List.copyOf(groups.values()));
    }

    /** Notifies that a stored group changed (persistence hook). */
    public void update(Group group) {
        // in-memory: nothing to do
    }
}
