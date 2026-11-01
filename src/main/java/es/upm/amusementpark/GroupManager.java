package es.upm.amusementpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Creates and deletes groups and changes their membership, keeping the storage
 * in sync. Locking rules that depend on tickets are checked by the caller.
 */
public class GroupManager {

    private final GroupStorage storage;
    private final List<Group> groups;

    public GroupManager(GroupStorage storage, Function<String, User> userByDni) throws IOException {
        this.storage = storage;
        this.groups = storage.load(userByDni);
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
            storage.save(groups);
        } catch (IOException e) {
            rollback.run();
            throw new OperationException("Could not save groups: " + e.getMessage());
        }
    }
}
