package es.upm.amusementpark;

import static es.upm.amusementpark.Checks.require;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the groups and persists every successful change; a change that cannot
 * be saved is undone. Lock rules depend on tickets and the system date and are
 * enforced by {@link ParkController}.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final List<Group> groups;

    public GroupManager(GroupRepository repository, UserManager users) throws IOException {
        this.repository = repository;
        this.groups = repository.loadAll(users::findByDni);
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group find(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    /** Returns the group with that id or rejects the operation. */
    public Group get(String id) {
        Group group = find(id);
        require(group != null, "group " + id + " does not exist");
        return group;
    }

    public Group create(String id, String name) throws IOException {
        require(UserValidator.isNotBlank(id), "group id must not be empty");
        require(UserValidator.isNotBlank(name), "group name must not be empty");
        require(find(id) == null, "group " + id + " already exists");
        Group group = new Group(id, name);
        groups.add(group);
        try {
            repository.saveAll(groups);
        } catch (IOException e) {
            groups.remove(group);
            throw e;
        }
        return group;
    }

    public void addMember(Group group, User user) throws IOException {
        require(!group.isMember(user), "user " + user.getEmail() + " is already in group " + group.getId());
        group.addMember(user);
        try {
            repository.saveAll(groups);
        } catch (IOException e) {
            group.removeMember(user);
            throw e;
        }
    }

    public void removeMember(Group group, User user) throws IOException {
        require(group.isMember(user), "user " + user.getEmail() + " is not in group " + group.getId());
        List<User> before = List.copyOf(group.getMembers());
        group.removeMember(user);
        try {
            repository.saveAll(groups);
        } catch (IOException e) {
            restore(group, before);
            throw e;
        }
    }

    public void delete(Group group) throws IOException {
        int index = groups.indexOf(group);
        groups.remove(index);
        try {
            repository.saveAll(groups);
        } catch (IOException e) {
            groups.add(index, group);
            throw e;
        }
    }

    /** Removes a deleted user from every group it belonged to. */
    public void removeFromAll(User user) throws IOException {
        boolean changed = false;
        for (Group group : groups) {
            if (group.isMember(user)) {
                group.removeMember(user);
                changed = true;
            }
        }
        if (changed) {
            repository.saveAll(groups);
        }
    }

    private static void restore(Group group, List<User> members) {
        for (User member : List.copyOf(group.getMembers())) {
            group.removeMember(member);
        }
        for (User member : members) {
            group.addMember(member);
        }
    }
}
