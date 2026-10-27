package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.persistence.GroupFileStore;

/**
 * Stores and retrieves user groups, indexed by identifier.
 */
public class GroupRepository {

    private final Map<String, Group> groupsById = new LinkedHashMap<>();
    private final GroupFileStore store;

    /** In-memory repository without persistence. */
    public GroupRepository() {
        this.store = null;
    }

    public GroupRepository(GroupFileStore store, UserRepository users) {
        this.store = store;
        for (Group group : store.load(users)) {
            groupsById.put(group.getId(), group);
        }
    }

    public void add(Group group) {
        groupsById.put(group.getId(), group);
        save();
    }

    /** Persists the current groups, including membership changes made on the group objects. */
    public void save() {
        if (store != null) {
            store.save(groupsById.values());
        }
    }

    public Group findById(String id) {
        return groupsById.get(id);
    }

    public boolean exists(String id) {
        return groupsById.containsKey(id);
    }

    public void remove(Group group) {
        groupsById.remove(group.getId());
        save();
    }

    public List<Group> findAll() {
        return new ArrayList<>(groupsById.values());
    }
}
