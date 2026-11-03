package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Group operations: creation and deletion of groups and management of their members, which must be
 * registered users. Persists the groups after every successful change.
 * Rule violations are reported with {@link IllegalArgumentException}.
 */
public class GroupController {

    private final GroupFileStorage storage;
    private final UserController users;
    private final List<Group> groups;

    public GroupController(GroupFileStorage storage, UserController users) {
        this.storage = storage;
        this.users = users;
        this.groups = new ArrayList<>(storage.load(users.getUsers()));
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Optional<Group> find(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public Group create(String id, String name) {
        require(UserValidator.isNotBlank(id), "Group identifier must not be empty");
        require(UserValidator.isNotBlank(name), "Group name must not be empty");
        require(find(id).isEmpty(), "A group with identifier " + id + " already exists");
        Group group = new Group(id, name);
        groups.add(group);
        saveOrRevert(() -> groups.remove(group));
        return group;
    }

    public void addMember(String id, String email) {
        Group group = get(id);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No registered user with email " + email));
        require(group.addMember(user), "User " + email + " is already in group " + id);
        saveOrRevert(() -> group.removeMember(user));
    }

    public void removeMember(String id, String email) {
        Group group = get(id);
        User user = group.getMembers().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User " + email + " is not in group " + id));
        List<User> before = new ArrayList<>(group.getMembers());
        group.removeMember(user);
        saveOrRevert(() -> restore(group, before));
    }

    public void delete(String id) {
        Group group = get(id);
        int index = groups.indexOf(group);
        groups.remove(index);
        saveOrRevert(() -> groups.add(index, group));
    }

    /** Removes a (deleted) user from every group it belongs to. */
    public void removeUserFromAll(User user) {
        boolean changed = false;
        for (Group g : groups) {
            changed |= g.removeMember(user);
        }
        if (changed) {
            storage.save(groups);
        }
    }

    private Group get(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("No group with identifier " + id));
    }

    private void saveOrRevert(Runnable revert) {
        try {
            storage.save(groups);
        } catch (RuntimeException e) {
            revert.run();
            throw e;
        }
    }

    private static void restore(Group group, List<User> members) {
        for (User u : new ArrayList<>(group.getMembers())) {
            group.removeMember(u);
        }
        members.forEach(group::addMember);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
