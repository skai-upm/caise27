package es.upm.parque.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.validation.UserValidator;
import es.upm.parque.persistence.GroupRepository;

/**
 * Manages groups of existing users (Singleton), including the lock imposed by current or future group tickets.
 */
public final class GroupController {

    private static GroupController instance;

    private final List<Group> groups = new ArrayList<>();
    private GroupRepository repository;
    private ParkController park;
    private UserController users;
    private TicketController tickets;

    private GroupController() {
    }

    public static synchronized GroupController getInstance() {
        if (instance == null) {
            instance = new GroupController();
        }
        return instance;
    }

    public void configure(GroupRepository repository, ParkController park, UserController users,
            TicketController tickets) {
        this.repository = repository;
        this.park = park;
        this.users = users;
        this.tickets = tickets;
    }

    /**
     * Loads the stored groups linked to the loaded users; users must be loaded first.
     */
    public void load() throws IOException {
        groups.clear();
        groups.addAll(repository.load(users::findByDni));
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public Group find(String id) {
        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        return null;
    }

    public String createGroup(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR Group identifier and name must not be empty";
        }
        if (find(id) != null) {
            return "ERROR Group " + id + " already exists";
        }
        groups.add(new Group(id, name));
        return persist() ? "OK GROUP CREATED " + id : "ERROR Could not save groups";
    }

    public String addToGroup(String groupId, String email) {
        Group group = find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR Unknown user: " + email;
        }
        if (tickets.isGroupLocked(group, park.getCurrentDate())) {
            return lockedError(group);
        }
        if (!group.addMember(user)) {
            return "ERROR " + user.getEmail() + " is already in group " + group.getId();
        }
        return persist() ? "OK USER ADDED TO GROUP " + group.getId() : "ERROR Could not save groups";
    }

    public String removeFromGroup(String groupId, String email) {
        Group group = find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        User user = users.findByEmail(email);
        if (user == null) {
            return "ERROR Unknown user: " + email;
        }
        if (tickets.isGroupLocked(group, park.getCurrentDate())) {
            return lockedError(group);
        }
        if (!group.removeMember(user)) {
            return "ERROR " + user.getEmail() + " is not in group " + group.getId();
        }
        return persist() ? "OK USER REMOVED FROM GROUP " + group.getId() : "ERROR Could not save groups";
    }

    /**
     * Deletes an unlocked group together with its past group tickets.
     */
    public String deleteGroup(String groupId) {
        Group group = find(groupId);
        if (group == null) {
            return "ERROR Unknown group: " + groupId;
        }
        if (tickets.isGroupLocked(group, park.getCurrentDate())) {
            return lockedError(group);
        }
        if (!tickets.removeGroupTicketsOf(group)) {
            return "ERROR Could not save changes";
        }
        groups.remove(group);
        return persist() ? "OK GROUP DELETED " + groupId : "ERROR Could not save groups";
    }

    /**
     * Removes the user from every group it belongs to.
     */
    public boolean removeUserEverywhere(User user) {
        for (Group group : groups) {
            group.removeMember(user);
        }
        return persist();
    }

    private String lockedError(Group group) {
        return "ERROR Group " + group.getId() + " has group tickets dated on or after " + park.getCurrentDate()
                + " and cannot be modified";
    }

    private boolean persist() {
        try {
            repository.saveAll(groups);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
