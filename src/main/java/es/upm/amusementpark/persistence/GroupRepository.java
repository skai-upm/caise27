package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves groups by identifier. When a {@link GroupFileStorage}
 * is given, groups are loaded from it on creation and saved to it after every
 * change.
 */
public class GroupRepository {

    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final GroupFileStorage storage;

    /** Creates a repository that keeps groups in memory only. */
    public GroupRepository() {
        this.storage = null;
    }

    public GroupRepository(GroupFileStorage storage, UserRepository users) {
        this.storage = storage;
        for (Group group : storage.load(users)) {
            groups.put(group.getIdentifier(), group);
        }
    }

    public void save(Group group) {
        groups.put(group.getIdentifier(), group);
        persist();
    }

    public Optional<Group> findById(String identifier) {
        return Optional.ofNullable(groups.get(identifier));
    }

    public boolean delete(String identifier) {
        boolean removed = groups.remove(identifier) != null;
        persist();
        return removed;
    }

    public List<Group> findAll() {
        return new ArrayList<>(groups.values());
    }

    /** Removes the user from every group it belongs to. */
    public void removeUserFromAll(User user) {
        for (Group group : groups.values()) {
            group.removeMember(user);
        }
        persist();
    }

    /** Writes the current groups to the storage, if any. */
    public void persist() {
        if (storage != null) {
            storage.save(groups.values());
        }
    }
}
