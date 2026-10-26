package es.upm.parque;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps the user groups and applies the group rules. Members are looked up in {@link UserManager}
 * so groups always hold the registered users. Every successful change is written through {@link GroupStorage}.
 */
public class GroupManager {

    private final Map<String, Group> groupsById = new LinkedHashMap<>();
    private final UserManager users;
    private final GroupStorage storage;

    public GroupManager(UserManager users, GroupStorage storage) {
        this.users = users;
        this.storage = storage;
        for (Group g : storage.load(users::findByDni)) {
            groupsById.put(g.getId(), g);
        }
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
        User user = users.findByEmail(email);
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
        User user = users.findByEmail(email);
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

    private void persist() {
        storage.save(new ArrayList<>(groupsById.values()));
    }
}
