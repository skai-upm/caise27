package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for groups of registered users. Every successful change is saved.
 */
public class GroupManager {

    private final GroupRepository repository;
    private final UserManager userManager;
    private final Map<String, Group> groups = new LinkedHashMap<>();

    public GroupManager(GroupRepository repository, UserManager userManager) throws IOException {
        this.repository = repository;
        this.userManager = userManager;
        for (Group group : repository.loadAll(userManager::findByDni)) {
            groups.put(group.getId(), group);
        }
    }

    public Group find(String id) {
        return groups.get(id);
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups.values());
    }

    public void create(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new UserException("The group identifier cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new UserException("The group name cannot be empty");
        }
        if (groups.containsKey(id)) {
            throw new UserException("A group with identifier " + id + " already exists");
        }
        groups.put(id, new Group(id, name));
        save();
    }

    public void addMember(String id, String email) {
        Group group = require(id);
        User user = userManager.findByEmail(email);
        if (user == null) {
            throw new UserException("No user with email " + email);
        }
        if (!group.addMember(user)) {
            throw new UserException("User " + email + " is already in group " + id);
        }
        save();
    }

    public void removeMember(String id, String email) {
        Group group = require(id);
        User user = userManager.findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            throw new UserException("User " + email + " is not in group " + id);
        }
        save();
    }

    public void delete(String id) {
        require(id);
        groups.remove(id);
        save();
    }

    /**
     * Removes a user from every group, e.g. after the user has been deleted.
     */
    public void removeFromAllGroups(User user) {
        boolean changed = false;
        for (Group group : groups.values()) {
            changed |= group.removeMember(user);
        }
        if (changed) {
            save();
        }
    }

    private Group require(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new UserException("No group with identifier " + id);
        }
        return group;
    }

    private void save() {
        try {
            repository.saveAll(groups.values());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save groups", e);
        }
    }
}
