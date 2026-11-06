package es.upm.park.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.park.model.Group;
import es.upm.park.model.User;
import es.upm.park.persistence.GroupFileStore;

/**
 * Keeps the groups known to the application, optionally backed by a file
 * store loaded on creation.
 */
public class GroupRepository {

    private final List<Group> groups = new ArrayList<>();
    private final GroupFileStore store;

    public GroupRepository() {
        this.store = null;
    }

    /** Loads stored groups, linking members to the existing users. */
    public GroupRepository(GroupFileStore store, Function<String, Optional<User>> userByDni) {
        this.store = store;
        groups.addAll(store.load(userByDni));
    }

    /** Adds a group, writing the file first when persistent. */
    public void add(Group group) {
        List<Group> snapshot = new ArrayList<>(groups);
        snapshot.add(group);
        persist(snapshot);
        groups.add(group);
    }

    /** Removes a group, writing the file first when persistent. */
    public void remove(Group group) {
        List<Group> snapshot = new ArrayList<>(groups);
        snapshot.remove(group);
        persist(snapshot);
        groups.remove(group);
    }

    /** Writes the current groups after an in-place change (membership). */
    public void save() {
        persist(groups);
    }

    public Optional<Group> findById(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public List<Group> findAll() {
        return Collections.unmodifiableList(groups);
    }

    private void persist(List<Group> snapshot) {
        if (store != null) {
            store.save(snapshot);
        }
    }
}
