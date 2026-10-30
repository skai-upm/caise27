package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.UserValidator;
import es.upm.amusementpark.persistence.GroupRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Group operations with persistence. Lock checks are done by {@link Park}, which knows the tickets.
 */
public class GroupService {

    private static final String SAVE_ERROR = "ERROR: could not save groups";

    private final GroupRepository repository;
    private final List<Group> groups;

    public GroupService(GroupRepository repository, List<User> users) throws IOException {
        this.repository = repository;
        this.groups = new ArrayList<>(repository.load(users));
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group findById(String id) {
        for (Group g : groups) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public String create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR: group identifier and name must not be empty";
        }
        if (findById(id) != null) {
            return "ERROR: group " + id + " already exists";
        }
        Group group = new Group(id, name);
        groups.add(group);
        if (!persist()) {
            groups.remove(group);
            return SAVE_ERROR;
        }
        return "OK: group " + id + " created";
    }

    public String delete(Group group) {
        int index = groups.indexOf(group);
        groups.remove(index);
        if (!persist()) {
            groups.add(index, group);
            return SAVE_ERROR;
        }
        return "OK: group " + group.getId() + " deleted";
    }

    public String addMember(Group group, User user) {
        if (!group.addMember(user)) {
            return "ERROR: user " + user.getEmail() + " is already in group " + group.getId();
        }
        if (!persist()) {
            group.removeMember(user);
            return SAVE_ERROR;
        }
        return "OK: user " + user.getEmail() + " added to group " + group.getId();
    }

    public String removeMember(Group group, User user) {
        if (!group.contains(user)) {
            return "ERROR: user " + user.getEmail() + " is not in group " + group.getId();
        }
        List<User> before = new ArrayList<>(group.getMembers());
        group.removeMember(user);
        if (!persist()) {
            restore(group, before);
            return SAVE_ERROR;
        }
        return "OK: user " + user.getEmail() + " removed from group " + group.getId();
    }

    /** Removes a (deleted) user from every group. */
    public void removeUserFromAll(User user) {
        boolean changed = false;
        for (Group g : groups) {
            changed |= g.removeMember(user);
        }
        if (changed && !persist()) {
            System.err.println("WARNING: could not save groups after removing user " + user.getEmail());
        }
    }

    private static void restore(Group group, List<User> members) {
        for (User u : new ArrayList<>(group.getMembers())) {
            group.removeMember(u);
        }
        for (User u : members) {
            group.addMember(u);
        }
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
