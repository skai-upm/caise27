package es.upm.parque.business.controller;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.GroupRepository;
import es.upm.parque.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Business rules for groups of users. Every successful change is saved through the repository.
 */
public class GroupController {
    private static final GroupController INSTANCE = new GroupController();

    private GroupRepository repository;
    private UserController userController;
    private final List<Group> groups = new ArrayList<>();
    private final List<Function<Group, String>> modificationChecks = new ArrayList<>();

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repository and the user controller, reloads the groups and discards previously registered checks.
     */
    public GroupController configure(GroupRepository repository, UserController userController) {
        this.repository = repository;
        this.userController = userController;
        groups.clear();
        groups.addAll(repository.load(userController::findByDni));
        modificationChecks.clear();
        return this;
    }

    /**
     * Registers a rule that can veto changing a group's members or deleting it: it returns the reason for refusing,
     * or null to allow it.
     */
    public void addModificationCheck(Function<Group, String> check) {
        modificationChecks.add(check);
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
        Group group = requireModifiable(id);
        groups.remove(group);
        repository.save(groups);
        return group;
    }

    public Group addMember(String id, String email) {
        Group group = requireModifiable(id);
        User user = userController.findByEmail(email);
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
        Group group = requireModifiable(id);
        User user = userController.findByEmail(email);
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

    private Group requireModifiable(String id) {
        Group group = require(id);
        for (Function<Group, String> check : modificationChecks) {
            String reason = check.apply(group);
            if (reason != null) {
                throw new IllegalArgumentException("group " + id + " cannot be modified: " + reason);
            }
        }
        return group;
    }

    private Group require(String id) {
        Group group = findById(id);
        if (group == null) {
            throw new IllegalArgumentException("group " + id + " does not exist");
        }
        return group;
    }
}
