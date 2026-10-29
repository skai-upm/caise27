package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.ticket.GroupTicket;
import es.upm.amusementpark.business.repository.GroupRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages groups and their membership; the lock state is derived from the group's tickets.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private final Map<String, Group> groups = new LinkedHashMap<>();
    private GroupRepository repository;
    private UserController users;
    private TicketController tickets;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(GroupRepository repository) {
        this.repository = repository;
    }

    public void setCollaborators(UserController users, TicketController tickets) {
        this.users = users;
        this.tickets = tickets;
    }

    public void load() throws PersistenceException {
        groups.clear();
        try {
            for (Group group : repository.load(users.usersByDni())) {
                groups.put(group.getId(), group);
            }
        } catch (IOException e) {
            throw new PersistenceException("could not load groups: " + e.getMessage(), e);
        }
    }

    public void save() throws PersistenceException {
        try {
            repository.saveAll(groups.values());
        } catch (IOException e) {
            throw new PersistenceException("could not save groups: " + e.getMessage(), e);
        }
    }

    public void create(String id, String name) throws ParkException {
        if (id.isEmpty()) {
            throw new ValidationException("group identifier must not be empty");
        }
        if (groups.containsKey(id)) {
            throw new DuplicateException("group already exists: " + id);
        }
        groups.put(id, new Group(id, name));
    }

    public void addMember(String groupId, String email) throws ParkException {
        Group group = requireGroup(groupId);
        User user = users.requireUser(email);
        requireUnlocked(group);
        if (group.hasMember(user)) {
            throw new DuplicateException("user already belongs to group " + groupId);
        }
        group.addMember(user);
    }

    public void removeMember(String groupId, String email) throws ParkException {
        Group group = requireGroup(groupId);
        User user = users.requireUser(email);
        requireUnlocked(group);
        if (!group.removeMember(user)) {
            throw new NotFoundException("user does not belong to group " + groupId);
        }
    }

    /**
     * Deletes a group without active group tickets; its historical group tickets lose the reference.
     */
    public void delete(String groupId) throws ParkException {
        Group group = requireGroup(groupId);
        if (isLocked(group)) {
            throw new OperationNotAllowedException("group has an active group ticket: " + groupId);
        }
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            ticket.detachGroup();
        }
        groups.remove(groupId);
    }

    /**
     * A group is locked while any of its group tickets is active; this is derived, never stored.
     */
    public boolean isLocked(Group group) {
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            if (tickets.isActive(ticket)) {
                return true;
            }
        }
        return false;
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.hasMember(user)) {
                result.add(group);
            }
        }
        return result;
    }

    public Group requireGroup(String id) throws NotFoundException {
        Group group = groups.get(id);
        if (group == null) {
            throw new NotFoundException("group not found: " + id);
        }
        return group;
    }

    public Group findGroup(String id) {
        return groups.get(id);
    }

    public Map<String, Group> groupsById() {
        return new LinkedHashMap<>(groups);
    }

    private void requireUnlocked(Group group) throws OperationNotAllowedException {
        if (isLocked(group)) {
            throw new OperationNotAllowedException("group has an active group ticket: " + group.getId());
        }
    }
}
