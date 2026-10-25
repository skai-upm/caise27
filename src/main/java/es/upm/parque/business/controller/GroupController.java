package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.GroupRepository;
import es.upm.parque.business.validation.UserValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the groups and their membership (Singleton). A group with a current or future group
 * ticket is locked.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private final List<Group> groups = new ArrayList<>();
    private GroupRepository repository;
    private UserController users;
    private TicketController tickets;
    private ParkController park;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(GroupRepository repository, UserController users, TicketController tickets,
                          ParkController park) {
        this.repository = repository;
        this.users = users;
        this.tickets = tickets;
        this.park = park;
    }

    public void load() {
        groups.clear();
        groups.addAll(repository.load(users::findByDni));
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Group find(String id) {
        return groups.stream().filter(group -> group.getId().equals(id)).findFirst().orElse(null);
    }

    public Group get(String id) {
        Group group = find(id);
        if (group == null) {
            throw new ParkException("group " + id + " does not exist");
        }
        return group;
    }

    public Group create(String id, String name) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ParkException("group identifier and name must not be empty");
        }
        if (find(id) != null) {
            throw new ParkException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        save();
        return group;
    }

    public void addMember(String groupId, String email) {
        Group group = unlocked(groupId);
        group.addMember(users.getByEmail(email));
        save();
    }

    public void removeMember(String groupId, String email) {
        Group group = unlocked(groupId);
        group.removeMember(users.getByEmail(email));
        save();
    }

    public void delete(String groupId) {
        groups.remove(unlocked(groupId));
        save();
    }

    /**
     * Removes the user from every group that contains it.
     */
    public void removeFromAll(User user) {
        boolean changed = false;
        for (Group group : groups) {
            if (group.hasMember(user)) {
                group.removeMember(user);
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    private Group unlocked(String groupId) {
        Group group = get(groupId);
        if (tickets.hasActiveGroupTicket(group)) {
            throw new ParkException("group " + groupId + " is locked by a group ticket dated on or after "
                    + park.getCurrentDate());
        }
        return group;
    }

    private void save() {
        repository.saveAll(groups);
    }
}
