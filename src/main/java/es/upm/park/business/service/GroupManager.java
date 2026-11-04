package es.upm.park.business.service;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
import es.upm.park.persistence.GroupFileStorage;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Group operations. Members are the {@link User} objects managed by {@link UserManager}.
 * Every successful change is saved; failed operations change neither memory nor file.
 */
public class GroupManager {

    private final GroupFileStorage storage;
    private final UserManager users;
    private Map<String, Group> groups = new LinkedHashMap<>();

    public GroupManager(GroupFileStorage storage, UserManager users) {
        this.storage = storage;
        this.users = users;
        for (Group g : storage.load(users::findByDni)) {
            groups.put(g.getId(), g);
        }
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public Group find(String id) {
        return groups.get(id);
    }

    public void create(String id, String name) {
        requireValid(UserValidator.isNonBlank(id), "invalid group identifier");
        requireValid(UserValidator.isNonBlank(name), "invalid group name");
        requireValid(!groups.containsKey(id), "a group with identifier " + id + " already exists");
        Map<String, Group> next = new LinkedHashMap<>(groups);
        next.put(id, new Group(id, name));
        commit(next);
    }

    public void delete(String id) {
        requireExisting(id);
        Map<String, Group> next = new LinkedHashMap<>(groups);
        next.remove(id);
        commit(next);
    }

    public void addMember(String id, String email) {
        Group group = requireExisting(id);
        User user = users.findByEmail(email);
        requireValid(user != null, "no registered user with email " + email);
        requireValid(!group.contains(user), "user " + email + " is already in group " + id);
        Group changed = group.copy();
        changed.add(user);
        replace(changed);
    }

    public void removeMember(String id, String email) {
        Group group = requireExisting(id);
        User user = users.findByEmail(email);
        requireValid(user != null && group.contains(user), "user " + email + " is not in group " + id);
        Group changed = group.copy();
        changed.remove(user);
        replace(changed);
    }

    /** Removes a deleted user from every group; saves only if some membership changed. */
    public void removeUserFromAllGroups(User user) {
        Map<String, Group> next = new LinkedHashMap<>();
        boolean changed = false;
        for (Group g : groups.values()) {
            Group copy = g.copy();
            changed |= copy.remove(user);
            next.put(copy.getId(), copy);
        }
        if (changed) {
            commit(next);
        }
    }

    private void replace(Group changed) {
        Map<String, Group> next = new LinkedHashMap<>(groups);
        next.put(changed.getId(), changed);
        commit(next);
    }

    private Group requireExisting(String id) {
        Group group = groups.get(id);
        requireValid(group != null, "no group with identifier " + id);
        return group;
    }

    private void commit(Map<String, Group> next) {
        storage.save(next.values());
        groups = next;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
