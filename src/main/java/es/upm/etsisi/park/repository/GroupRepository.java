package es.upm.etsisi.park.repository;

import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.GroupFileStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * In-memory collection of groups, persisted through a {@link GroupFileStore}.
 */
public class GroupRepository {

    private final GroupFileStore store;
    private final List<Group> groups;

    public GroupRepository(GroupFileStore store, UserRepository users) {
        this.store = store;
        this.groups = new ArrayList<>(store.load(users::findByDni));
    }

    public Optional<Group> findById(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }

    public List<Group> findByMember(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    public List<Group> findAll() {
        return Collections.unmodifiableList(groups);
    }

    public void add(Group group) {
        groups.add(group);
        save();
    }

    public void remove(Group group) {
        groups.remove(group);
        save();
    }

    public void save() {
        store.save(groups);
    }
}
