package com.amusementpark.business.controller;

import com.amusementpark.persistence.GroupRepository;
import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.User;
import com.amusementpark.business.model.UserValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Group operations with persistence after each change. Ticket locks are checked by the caller.
 */
public class GroupController {

    private static final String SAVE_FAILED = "ERROR: Could not save groups file.";

    private static final GroupController INSTANCE = new GroupController();

    private GroupRepository storage;
    private final List<Group> groups = new ArrayList<>();

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and the user controller whose users the groups reference, then loads. */
    public void configure(GroupRepository storage, UserController users) throws IOException {
        this.storage = storage;
        groups.clear();
        groups.addAll(storage.load(users.getUsers()));
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    public Group find(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public String create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR: Group identifier and name must not be empty.";
        }
        if (find(id) != null) {
            return "ERROR: Group " + id + " already exists.";
        }
        Group group = new Group(id, name);
        groups.add(group);
        if (!persist()) {
            groups.remove(group);
            return SAVE_FAILED;
        }
        return "OK: Group created: " + id;
    }

    public String addMember(Group group, User user) {
        if (group.contains(user)) {
            return "ERROR: " + user.getEmail() + " is already in group " + group.getId() + ".";
        }
        group.add(user);
        if (!persist()) {
            group.remove(user);
            return SAVE_FAILED;
        }
        return "OK: " + user.getEmail() + " added to group " + group.getId();
    }

    public String removeMember(Group group, User user) {
        if (!group.contains(user)) {
            return "ERROR: " + user.getEmail() + " is not in group " + group.getId() + ".";
        }
        group.remove(user);
        if (!persist()) {
            group.add(user);
            return SAVE_FAILED;
        }
        return "OK: " + user.getEmail() + " removed from group " + group.getId();
    }

    public String delete(Group group) {
        int index = groups.indexOf(group);
        groups.remove(index);
        if (!persist()) {
            groups.add(index, group);
            return SAVE_FAILED;
        }
        return "OK: Group deleted: " + group.getId();
    }

    /**
     * Removes the user from every group and saves.
     *
     * @return the groups the user was removed from, or {@code null} if saving failed (nothing changed)
     */
    public List<Group> removeFromAll(User user) {
        List<Group> changed = new ArrayList<>();
        for (Group g : groups) {
            if (g.contains(user)) {
                changed.add(g);
                g.remove(user);
            }
        }
        if (!changed.isEmpty() && !persist()) {
            restore(user, changed);
            return null;
        }
        return changed;
    }

    /** Re-adds the user to the given groups after a failed user deletion. */
    public void restore(User user, List<Group> memberships) {
        for (Group g : memberships) {
            g.add(user);
        }
        persist();
    }

    /** Saves the current state; used after user data that groups reference has changed. */
    public boolean persist() {
        try {
            storage.save(groups);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
