package es.upm.amusementpark.repository;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.persistence.GroupStorage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves groups by identifier, keeping the configured storage up to date.
 */
public class GroupRepository {

    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final GroupStorage storage;

    public GroupRepository() {
        this(GroupStorage.none(), new UserRepository());
    }

    public GroupRepository(GroupStorage storage, UserRepository users) {
        this.storage = storage;
        storage.load(users).forEach(g -> groups.put(g.getIdentifier(), g));
    }

    public void add(Group group) {
        groups.put(group.getIdentifier(), group);
        save();
    }

    public Optional<Group> findById(String identifier) {
        return Optional.ofNullable(groups.get(identifier));
    }

    public void remove(Group group) {
        groups.remove(group.getIdentifier());
        save();
    }

    public List<Group> findAll() {
        return List.copyOf(groups.values());
    }

    /** Persists changes made to a stored group. */
    public void update(Group group) {
        save();
    }

    private void save() {
        storage.save(groups.values());
    }
}
