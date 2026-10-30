package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;

/**
 * Group operations (Singleton). Membership changes and deletion are refused while the group is locked by an active
 * group ticket.
 */
public final class GroupController {

    private static GroupController instance;

    private Park park;
    private GroupRepository groups;
    private UserRepository users;
    private TicketRepository tickets;

    private GroupController() {
    }

    public static synchronized GroupController getInstance() {
        if (instance == null) {
            instance = new GroupController();
        }
        return instance;
    }

    public void configure(Park park, GroupRepository groups, UserRepository users, TicketRepository tickets) {
        this.park = park;
        this.groups = groups;
        this.users = users;
        this.tickets = tickets;
    }

    public Group create(String id, String name) {
        if (groups.findById(id) != null) {
            throw new DuplicateException("a group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        return group;
    }

    public void add(String groupId, String email) {
        Group group = modifiableGroup(groupId);
        group.addMember(existingUser(email));
        groups.save();
    }

    public void remove(String groupId, String email) {
        Group group = modifiableGroup(groupId);
        group.removeMember(existingUser(email));
        groups.save();
    }

    /**
     * Deletes the group. Its historical group tickets are kept with a null group reference.
     */
    public void delete(String groupId) {
        Group group = modifiableGroup(groupId);
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            ticket.detachGroup();
        }
        tickets.save();
        groups.remove(group);
    }

    public Group existing(String groupId) {
        Group group = groups.findById(groupId);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + groupId);
        }
        return group;
    }

    private Group modifiableGroup(String groupId) {
        Group group = existing(groupId);
        if (park.isLocked(group)) {
            throw new OperationNotAllowedException("group " + groupId + " is locked by an active group ticket");
        }
        return group;
    }

    private User existingUser(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        return user;
    }
}
