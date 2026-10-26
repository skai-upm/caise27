package es.upm.parque;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for groups of users. Every successful change is saved through the repository.
 */
public class GroupManager {
    private final GroupRepository repository;
    private final UserManager userManager;
    private final List<Group> groups;

    public GroupManager(GroupRepository repository, UserManager userManager) {
        this.repository = repository;
        this.userManager = userManager;
        this.groups = new ArrayList<>(repository.load(userManager::findByDni));
    }

    public Group create(String id, String name) {
        if (!UserValidator.isNotBlank(id)) {
            throw new IllegalArgumentException("the group identifier cannot be empty");
        }
        if (!UserValidator.isNotBlank(name)) {
            throw new IllegalArgumentException("the group name cannot be empty");
        }
        if (findById(id) != null) {
            throw new IllegalArgumentException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        repository.save(groups);
        return group;
    }

    public Group delete(String id) {
        Group group = require(id);
        groups.remove(group);
        repository.save(groups);
        return group;
    }

    public Group addMember(String id, String email) {
        Group group = require(id);
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        if (!group.addMember(user)) {
            throw new IllegalArgumentException(email + " is already a member of group " + id);
        }
        repository.save(groups);
        return group;
    }

    public Group removeMember(String id, String email) {
        Group group = require(id);
        User user = userManager.findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            throw new IllegalArgumentException(email + " is not a member of group " + id);
        }
        repository.save(groups);
        return group;
    }

    public void removeUserFromAllGroups(User user) {
        boolean changed = false;
        for (Group g : groups) {
            changed |= g.removeMember(user);
        }
        if (changed) {
            repository.save(groups);
        }
    }

    public Group findById(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    private Group require(String id) {
        Group group = findById(id);
        if (group == null) {
            throw new IllegalArgumentException("group " + id + " does not exist");
        }
        return group;
    }
}
