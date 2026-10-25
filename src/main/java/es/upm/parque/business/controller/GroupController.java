package es.upm.parque.business.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.GroupRepository;
import es.upm.parque.business.validation.Identifiers;
import es.upm.parque.business.validation.UserValidator;

/**
 * Groups and their membership rules, including the lock imposed by current or future group
 * tickets. Singleton.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private final Map<String, Group> groups = new LinkedHashMap<>();
    private GroupRepository repository;
    private ParkController park;
    private UserController users;
    private TicketController tickets;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(GroupRepository repository, ParkController park, UserController users,
                          TicketController tickets) {
        this.repository = repository;
        this.park = park;
        this.users = users;
        this.tickets = tickets;
    }

    public void load() {
        groups.clear();
        for (Group g : repository.load(users::findByDni)) {
            groups.put(g.getId(), g);
        }
    }

    public List<Group> getGroups() {
        return List.copyOf(groups.values());
    }

    public Group find(String id) {
        return groups.get(id);
    }

    public Group get(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + id);
        }
        return group;
    }

    public Group create(String id, String name) {
        Identifiers.require(id, "group");
        if (!UserValidator.isNotEmpty(name)) {
            throw new IllegalArgumentException("Empty group name");
        }
        if (groups.containsKey(id)) {
            throw new IllegalArgumentException("Group already exists: " + id);
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        save();
        return group;
    }

    public void addMember(String groupId, String email) {
        Group group = unlocked(groupId);
        User user = users.get(email);
        if (group.contains(user)) {
            throw new IllegalArgumentException("User " + user.getEmail() + " is already in group " + groupId);
        }
        group.addMember(user);
        save();
    }

    public void removeMember(String groupId, String email) {
        Group group = unlocked(groupId);
        User user = users.get(email);
        if (!group.removeMember(user)) {
            throw new IllegalArgumentException("User " + user.getEmail() + " is not in group " + groupId);
        }
        save();
    }

    public void delete(String groupId) {
        groups.remove(unlocked(groupId).getId());
        save();
    }

    /**
     * Removes the user from every group it belongs to (used when the user is deleted).
     */
    public void removeUserEverywhere(User user) {
        boolean changed = false;
        for (Group g : groups.values()) {
            changed |= g.removeMember(user);
        }
        if (changed) {
            save();
        }
    }

    private Group unlocked(String groupId) {
        Group group = get(groupId);
        if (tickets.isGroupLocked(group)) {
            throw new IllegalArgumentException("Group " + groupId + " is locked by a group ticket dated on or after "
                    + park.getSystemDate());
        }
        return group;
    }

    private void save() {
        repository.save(groups.values());
    }
}
