package es.upm.parque.business.controller;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.GroupRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for groups of registered users. Every successful change is saved.
 */
public class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private GroupRepository repository;
    private UserController userController;
    private final Map<String, Group> groups = new LinkedHashMap<>();

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators of the controller and (re)loads its data. Called during application
     * initialization.
     */
    public GroupController configure(GroupRepository repository, UserController userController) throws IOException {
        this.repository = repository;
        this.userController = userController;
        groups.clear();
        for (Group group : repository.loadAll(userController::findByDni)) {
            groups.put(group.getId(), group);
        }
        return this;
    }

    public Group find(String id) {
        requireConfigured();
        return groups.get(id);
    }

    public List<Group> getGroups() {
        requireConfigured();
        return new ArrayList<>(groups.values());
    }

    public void create(String id, String name) {
        requireConfigured();
        if (id == null || id.isBlank()) {
            throw new BusinessException("The group identifier cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("The group name cannot be empty");
        }
        if (groups.containsKey(id)) {
            throw new BusinessException("A group with identifier " + id + " already exists");
        }
        groups.put(id, new Group(id, name));
        save();
    }

    public void addMember(String id, String email) {
        requireConfigured();
        Group group = require(id);
        User user = userController.findByEmail(email);
        if (user == null) {
            throw new BusinessException("No user with email " + email);
        }
        if (!group.addMember(user)) {
            throw new BusinessException("User " + email + " is already in group " + id);
        }
        save();
    }

    public void removeMember(String id, String email) {
        requireConfigured();
        Group group = require(id);
        User user = userController.findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            throw new BusinessException("User " + email + " is not in group " + id);
        }
        save();
    }

    public void delete(String id) {
        requireConfigured();
        require(id);
        groups.remove(id);
        save();
    }

    /**
     * Removes a user from every group, e.g. after the user has been deleted.
     */
    public void removeFromAllGroups(User user) {
        requireConfigured();
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
            throw new BusinessException("No group with identifier " + id);
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

    private void requireConfigured() {
        if (repository == null) {
            throw new IllegalStateException("GroupController is not configured");
        }
    }
}
