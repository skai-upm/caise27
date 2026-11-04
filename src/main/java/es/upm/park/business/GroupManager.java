package es.upm.park.business;

import es.upm.park.business.model.Result;
import es.upm.park.business.model.Group;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Creates and deletes groups and manages their members. Every successful change is saved
 * before it becomes visible in memory.
 */
public class GroupManager {

    private final GroupFileStore store;
    private final UserManager users;
    private List<Group> groups;

    public GroupManager(GroupFileStore store, UserManager users) throws IOException {
        this.store = store;
        this.users = users;
        this.groups = new ArrayList<>(store.load(users::findByDni));
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Optional<Group> find(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst();
    }

    public Result create(String id, String name) {
        if (!UserValidator.isNonBlank(id) || !UserValidator.isNonBlank(name)) {
            return Result.error("Group identifier and name must not be empty.");
        }
        if (find(id).isPresent()) {
            return Result.error("A group with identifier " + id + " already exists.");
        }
        List<Group> candidate = new ArrayList<>(groups);
        candidate.add(new Group(id, name));
        return commit(candidate, "Group " + id + " created.");
    }

    public Result delete(String id) {
        int index = indexOf(id);
        if (index < 0) {
            return Result.error("Group " + id + " does not exist.");
        }
        List<Group> candidate = new ArrayList<>(groups);
        candidate.remove(index);
        return commit(candidate, "Group " + id + " deleted.");
    }

    public Result addMember(String id, String email) {
        int index = indexOf(id);
        if (index < 0) {
            return Result.error("Group " + id + " does not exist.");
        }
        Optional<User> user = users.findByEmail(email);
        if (user.isEmpty()) {
            return Result.error("No registered user with email " + email + ".");
        }
        Group changed = new Group(groups.get(index));
        if (!changed.addMember(user.get())) {
            return Result.error("User " + email + " is already in group " + id + ".");
        }
        return replace(index, changed, "User " + email + " added to group " + id + ".");
    }

    public Result removeMember(String id, String email) {
        int index = indexOf(id);
        if (index < 0) {
            return Result.error("Group " + id + " does not exist.");
        }
        Optional<User> user = users.findByEmail(email);
        Group changed = new Group(groups.get(index));
        if (user.isEmpty() || !changed.removeMember(user.get())) {
            return Result.error("User " + email + " is not in group " + id + ".");
        }
        return replace(index, changed, "User " + email + " removed from group " + id + ".");
    }

    /** Removes a (deleted) user from every group that contains it. */
    public Result removeUserEverywhere(User user) {
        List<Group> candidate = new ArrayList<>();
        boolean changed = false;
        for (Group g : groups) {
            if (g.contains(user)) {
                Group copy = new Group(g);
                copy.removeMember(user);
                candidate.add(copy);
                changed = true;
            } else {
                candidate.add(g);
            }
        }
        return changed ? commit(candidate, "User removed from its groups.") : Result.ok("User was in no group.");
    }

    private int indexOf(String id) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private Result replace(int index, Group changed, String message) {
        List<Group> candidate = new ArrayList<>(groups);
        candidate.set(index, changed);
        return commit(candidate, message);
    }

    private Result commit(List<Group> candidate, String message) {
        try {
            store.save(candidate);
        } catch (IOException e) {
            return Result.error("Could not save groups: " + e.getMessage());
        }
        groups = candidate;
        return Result.ok(message);
    }
}
