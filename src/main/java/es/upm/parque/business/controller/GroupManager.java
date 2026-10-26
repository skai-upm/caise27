package es.upm.parque.business.controller;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.GroupRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps the user groups and applies the group rules. Members are looked up in {@link UserManager}
 * so groups always hold the registered users. Every successful change is written through {@link GroupRepository}.
 */
public class GroupManager {

    private final Map<String, Group> groupsById = new LinkedHashMap<>();

    private static final GroupManager INSTANCE = new GroupManager();

    private UserManager users;
    private GroupRepository repository;

    private GroupManager() {
    }

    public static GroupManager getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators and reloads the groups from the repository, replacing any previous state.
     * The user manager must already be configured.
     */
    public GroupManager configure(UserManager users, GroupRepository repository) {
        this.users = users;
        this.repository = repository;
        groupsById.clear();
        for (Group g : repository.load(users::findByDni)) {
            groupsById.put(g.getId(), g);
        }
        return this;
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groupsById.values());
    }

    public Group find(String id) {
        return groupsById.get(id);
    }

    public Group create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new IllegalArgumentException("The group identifier and name cannot be empty");
        }
        if (groupsById.containsKey(id)) {
            throw new IllegalArgumentException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        groupsById.put(id, group);
        persist();
        return group;
    }

    public void delete(String id) {
        if (groupsById.remove(require(id).getId()) != null) {
            persist();
        }
    }

    public void addMember(String id, String email) {
        Group group = require(id);
        User user = users().findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        if (!group.addMember(user)) {
            throw new IllegalArgumentException("User " + email + " is already in group " + id);
        }
        persist();
    }

    public void removeMember(String id, String email) {
        Group group = require(id);
        User user = users().findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            throw new IllegalArgumentException("User " + email + " is not in group " + id);
        }
        persist();
    }

    /** Removes a user from every group, used when the user is deleted. */
    public void removeUserFromAll(User user) {
        boolean changed = false;
        for (Group g : groupsById.values()) {
            changed |= g.removeMember(user);
        }
        if (changed) {
            persist();
        }
    }

    private Group require(String id) {
        Group group = groupsById.get(id);
        if (group == null) {
            throw new IllegalArgumentException("No group with identifier " + id);
        }
        return group;
    }

    private UserManager users() {
        if (users == null) {
            throw new IllegalStateException("GroupManager is not configured");
        }
        return users;
    }

    private GroupRepository repository() {
        if (repository == null) {
            throw new IllegalStateException("GroupManager is not configured");
        }
        return repository;
    }

    private void persist() {
        repository().save(new ArrayList<>(groupsById.values()));
    }
}
