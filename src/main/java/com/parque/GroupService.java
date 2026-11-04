package com.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Group operations. Members are always the registered users obtained from {@link UserService}.
 * Every rejected operation throws {@link IllegalArgumentException} and leaves memory and file unchanged.
 */
public class GroupService {
    private final GroupRepository repository;
    private final UserService users;
    private List<Group> groups;

    public GroupService(GroupRepository repository, UserService users) throws IOException {
        this.repository = repository;
        this.users = users;
        this.groups = new ArrayList<>(repository.load(users::findByDni));
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group find(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public void create(String id, String name) {
        require(UserValidator.isNonBlank(id), "Group identifier must not be empty");
        require(UserValidator.isNonBlank(name), "Group name must not be empty");
        require(!id.contains(","), "Group identifier must not contain ','");
        require(find(id) == null, "A group with identifier " + id + " already exists");
        List<Group> next = new ArrayList<>(groups);
        next.add(new Group(id, name));
        save(next);
        groups = next;
    }

    public void addMember(String id, String email) {
        Group group = existing(id);
        User user = registeredUser(email);
        require(!group.contains(user), "User " + email + " is already in group " + id);
        Group changed = new Group(group);
        changed.addMember(user);
        saveReplacing(group, changed);
        group.addMember(user);
    }

    public void removeMember(String id, String email) {
        Group group = existing(id);
        User user = registeredUser(email);
        require(group.contains(user), "User " + email + " is not in group " + id);
        Group changed = new Group(group);
        changed.removeMember(user);
        saveReplacing(group, changed);
        group.removeMember(user);
    }

    public void delete(String id) {
        Group group = existing(id);
        List<Group> next = new ArrayList<>(groups);
        next.remove(group);
        save(next);
        groups = next;
    }

    /** Removes a (deleted) user from every group that contains it. */
    public void removeUserFromAllGroups(User user) {
        List<Group> toSave = new ArrayList<>();
        boolean changed = false;
        for (Group g : groups) {
            if (g.contains(user)) {
                Group copy = new Group(g);
                copy.removeMember(user);
                toSave.add(copy);
                changed = true;
            } else {
                toSave.add(g);
            }
        }
        if (!changed) {
            return;
        }
        save(toSave);
        for (Group g : groups) {
            g.removeMember(user);
        }
    }

    private Group existing(String id) {
        Group group = find(id);
        require(group != null, "No group with identifier " + id);
        return group;
    }

    private User registeredUser(String email) {
        User user = users.findByEmail(email);
        require(user != null, "No user with email " + email);
        return user;
    }

    private void saveReplacing(Group original, Group changed) {
        List<Group> toSave = new ArrayList<>(groups);
        toSave.set(toSave.indexOf(original), changed);
        save(toSave);
    }

    private void save(List<Group> state) {
        try {
            repository.save(state);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save groups: " + e.getMessage(), e);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
