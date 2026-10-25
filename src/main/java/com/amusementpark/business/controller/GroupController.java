package com.amusementpark.business.controller;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.User;
import com.amusementpark.business.validation.Validator;
import com.amusementpark.persistence.GroupRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages groups of registered users and their membership locks.
 */
public final class GroupController {
    private static final GroupController INSTANCE = new GroupController();

    private GroupRepository repository;
    private UserController userController;
    private TicketController ticketController;
    private final List<Group> groups = new ArrayList<>();

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(GroupRepository repository, UserController userController,
                          TicketController ticketController) {
        this.repository = repository;
        this.userController = userController;
        this.ticketController = ticketController;
        groups.clear();
    }

    public void load() throws IOException {
        groups.clear();
        groups.addAll(repository.load(userController.getUsers()));
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group findGroup(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public String create(String id, String name) {
        if (!Validator.isValidId(id)) {
            return "ERROR: invalid group id";
        }
        if (name.isBlank()) {
            return "ERROR: group name is required";
        }
        if (findGroup(id) != null) {
            return "ERROR: group " + id + " already exists";
        }
        Group group = new Group(id, name);
        groups.add(group);
        if (!persist()) {
            groups.remove(group);
            return "ERROR: could not save groups";
        }
        return "OK: group created";
    }

    public String addMember(String id, String email) {
        Group group = findGroup(id);
        if (group == null) {
            return "ERROR: unknown group " + id;
        }
        User user = userController.findByEmail(email);
        if (user == null) {
            return "ERROR: unknown user " + email;
        }
        if (isLocked(group)) {
            return lockedMessage(group);
        }
        if (!group.add(user)) {
            return "ERROR: user is already in group " + id;
        }
        if (!persist()) {
            group.remove(user);
            return "ERROR: could not save groups";
        }
        return "OK: user added to group";
    }

    public String removeMember(String id, String email) {
        Group group = findGroup(id);
        if (group == null) {
            return "ERROR: unknown group " + id;
        }
        User user = userController.findByEmail(email);
        if (user == null || !group.contains(user)) {
            return "ERROR: user is not in group " + id;
        }
        if (isLocked(group)) {
            return lockedMessage(group);
        }
        group.remove(user);
        if (!persist()) {
            group.add(user);
            return "ERROR: could not save groups";
        }
        return "OK: user removed from group";
    }

    public String delete(String id) {
        Group group = findGroup(id);
        if (group == null) {
            return "ERROR: unknown group " + id;
        }
        if (isLocked(group)) {
            return lockedMessage(group);
        }
        int index = groups.indexOf(group);
        groups.remove(index);
        if (!persist()) {
            groups.add(index, group);
            return "ERROR: could not save groups";
        }
        return "OK: group deleted";
    }

    /**
     * Removes a deleted user from every group; returns false if the change could not be saved.
     */
    boolean removeFromAllGroups(User user) {
        boolean changed = false;
        for (Group group : groups) {
            changed |= group.remove(user);
        }
        return !changed || persist();
    }

    /**
     * A group is locked while it has a group ticket dated on or after the system date.
     */
    public boolean isLocked(Group group) {
        return ticketController.hasActiveGroupTicket(group);
    }

    private String lockedMessage(Group group) {
        return "ERROR: group " + group.getId() + " has a group ticket dated on or after "
                + ParkController.getInstance().getCurrentDate();
    }

    private boolean persist() {
        try {
            repository.save(groups);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
