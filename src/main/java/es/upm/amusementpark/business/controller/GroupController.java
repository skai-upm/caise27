package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.GroupRepository;

/**
 * Creates and deletes groups and changes their membership, keeping the repository
 * in sync. Locking rules that depend on tickets are checked by the caller.
 */
public class GroupController {

    private static GroupController instance;

    /**
     * Creates the single instance for this execution. Fails if it already exists.
     */
    public static synchronized GroupController initialize(GroupRepository repository, UserController users) throws IOException {
        if (instance != null) {
            throw new IllegalStateException("GroupController is already initialized");
        }
        instance = new GroupController(repository, users);
        return instance;
    }

    public static synchronized GroupController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GroupController is not initialized");
        }
        return instance;
    }

    /**
     * Discards the instance at the end of an execution.
     */
    public static synchronized void reset() {
        instance = null;
    }

    private final GroupRepository repository;
    private final List<Group> groups;

    private GroupController(GroupRepository repository, UserController users) throws IOException {
        this.repository = repository;
        this.groups = repository.load(users::findByDni);
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group find(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public Group require(String id) throws OperationException {
        Group group = find(id);
        if (group == null) {
            throw new OperationException("Unknown group: " + id);
        }
        return group;
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group g : groups) {
            if (g.hasMember(user)) {
                result.add(g);
            }
        }
        return result;
    }

    public void create(String id, String name) throws OperationException {
        if (id.isEmpty()) {
            throw new OperationException("Group identifier must not be empty");
        }
        if (name.isEmpty()) {
            throw new OperationException("Group name must not be empty");
        }
        if (find(id) != null) {
            throw new OperationException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        save(() -> groups.remove(group));
    }

    public void addMember(Group group, User user) throws OperationException {
        if (group.hasMember(user)) {
            throw new OperationException("User " + user.getEmail() + " is already in group " + group.getId());
        }
        group.addMember(user);
        save(() -> group.removeMember(user));
    }

    public void removeMember(Group group, User user) throws OperationException {
        if (!group.hasMember(user)) {
            throw new OperationException("User " + user.getEmail() + " is not in group " + group.getId());
        }
        List<User> before = group.getMembers();
        group.removeMember(user);
        save(() -> restore(group, before));
    }

    public void delete(Group group) throws OperationException {
        int index = groups.indexOf(group);
        groups.remove(index);
        save(() -> groups.add(index, group));
    }

    /**
     * Removes a user from every group, used when the user is deleted.
     */
    public void removeUserEverywhere(User user) throws OperationException {
        List<Group> affected = groupsOf(user);
        if (affected.isEmpty()) {
            return;
        }
        List<List<User>> before = new ArrayList<>();
        for (Group g : affected) {
            before.add(g.getMembers());
            g.removeMember(user);
        }
        save(() -> {
            for (int i = 0; i < affected.size(); i++) {
                restore(affected.get(i), before.get(i));
            }
        });
    }

    private static void restore(Group group, List<User> members) {
        for (User u : group.getMembers()) {
            group.removeMember(u);
        }
        for (User u : members) {
            group.addMember(u);
        }
    }

    private void save(Runnable rollback) throws OperationException {
        try {
            repository.save(groups);
        } catch (IOException e) {
            rollback.run();
            throw new OperationException("Could not save groups: " + e.getMessage());
        }
    }
}
