package es.upm.etsisi.amusementpark.repository;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves groups, keeping the backing file in sync with every change.
 */
public class GroupRepository {

    private final GroupFileStore store;
    private final List<Group> groups;

    public GroupRepository(GroupFileStore store) {
        this.store = store;
        this.groups = new ArrayList<>(store.load());
    }

    public Optional<Group> findById(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public List<Group> findByMember(User user) {
        return groups.stream().filter(g -> g.contains(user)).toList();
    }

    public List<Group> findAll() {
        return List.copyOf(groups);
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
