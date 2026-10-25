package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Keeps the groups and their membership, persisting after every change.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final List<Group> groups;

    public GroupManager(GroupRepository repository, Function<String, User> usersByDni) {
        this.repository = repository;
        this.groups = new ArrayList<>(repository.load(usersByDni));
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group find(String id) {
        return groups.stream().filter(group -> group.getId().equals(id)).findFirst().orElse(null);
    }

    public Group get(String id) {
        Group group = find(id);
        if (group == null) {
            throw new ParkException("group " + id + " does not exist");
        }
        return group;
    }

    public Group create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ParkException("group identifier and name must not be empty");
        }
        if (find(id) != null) {
            throw new ParkException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        save();
        return group;
    }

    public void addMember(Group group, User user) {
        group.addMember(user);
        save();
    }

    public void removeMember(Group group, User user) {
        group.removeMember(user);
        save();
    }

    public void delete(Group group) {
        groups.remove(group);
        save();
    }

    /**
     * Removes the user from every group that contains it.
     */
    public void removeFromAll(User user) {
        boolean changed = false;
        for (Group group : groups) {
            if (group.hasMember(user)) {
                group.removeMember(user);
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    private void save() {
        repository.saveAll(groups);
    }
}
