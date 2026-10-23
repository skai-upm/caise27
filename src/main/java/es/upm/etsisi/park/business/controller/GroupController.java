package es.upm.etsisi.park.business.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.persistence.GroupRepository;

/**
 * Manages user groups. A group is locked while it has an active group
 * ticket; the lock is derived from the tickets, never stored. Singleton.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private GroupRepository repository;
    private SystemController system;
    private UserController users;
    private TicketController tickets;
    private final List<Group> groups = new ArrayList<>();

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(GroupRepository repository, SystemController system, UserController users,
                          TicketController tickets) {
        this.repository = repository;
        this.system = system;
        this.users = users;
        this.tickets = tickets;
        groups.clear();
    }

    public void load() throws PersistenceException {
        groups.clear();
        try {
            groups.addAll(repository.load(users.usersByDni()));
        } catch (IOException e) {
            throw new PersistenceException("Could not load groups: " + e.getMessage(), e);
        }
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Map<String, Group> groupsById() {
        Map<String, Group> byId = new HashMap<>();
        for (Group group : groups) {
            byId.put(group.getId(), group);
        }
        return byId;
    }

    public Group find(String id) {
        for (Group group : groups) {
            if (group.getId().equalsIgnoreCase(id)) {
                return group;
            }
        }
        return null;
    }

    public Group require(String id) throws NotFoundException {
        Group group = find(id);
        if (group == null) {
            throw new NotFoundException("Group " + id + " not found");
        }
        return group;
    }

    public Group create(String id, String name) throws ParkException {
        Checks.requireText(id, "Group identifier");
        Checks.requireText(name, "Group name");
        if (find(id) != null) {
            throw new DuplicateException("Group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        persist();
        return group;
    }

    public void addMember(String groupId, String email) throws ParkException {
        Group group = require(groupId);
        User user = users.require(email);
        if (group.contains(user)) {
            throw new DuplicateException("User " + email + " already belongs to group " + group.getId());
        }
        requireUnlocked(group);
        group.addMember(user);
        persist();
    }

    public void removeMember(String groupId, String email) throws ParkException {
        Group group = require(groupId);
        User user = users.require(email);
        if (!group.contains(user)) {
            throw new NotFoundException("User " + email + " does not belong to group " + group.getId());
        }
        requireUnlocked(group);
        group.removeMember(user);
        persist();
    }

    /** Deletes an unlocked group; its historical group tickets keep their data but lose the reference. */
    public void delete(String groupId) throws ParkException {
        Group group = require(groupId);
        requireUnlocked(group);
        groups.remove(group);
        persist();
        tickets.detachGroup(group);
    }

    /** A group is locked while any of its group tickets is dated today or later. */
    public boolean isLocked(Group group) {
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            if (ticket.isActiveOn(system.getDate())) {
                return true;
            }
        }
        return false;
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }

    /** Called when a user is deleted: keeps the groups consistent. */
    void removeFromAllGroups(User user) throws PersistenceException {
        boolean changed = false;
        for (Group group : groups) {
            changed |= group.removeMember(user);
        }
        if (changed) {
            persist();
        }
    }

    private void requireUnlocked(Group group) throws OperationNotAllowedException {
        if (isLocked(group)) {
            throw new OperationNotAllowedException("Group " + group.getId() + " is locked by an active group ticket");
        }
    }

    private void persist() throws PersistenceException {
        try {
            repository.save(groups);
        } catch (IOException e) {
            throw new PersistenceException("Could not save groups: " + e.getMessage(), e);
        }
    }
}
