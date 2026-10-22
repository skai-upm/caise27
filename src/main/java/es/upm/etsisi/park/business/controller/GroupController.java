package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.UserRepository;

/**
 * GROUP CREATE / ADD / REMOVE / DELETE. A group is locked while it has an active group
 * ticket; the lock is always derived from the tickets and the current date. Single
 * instance per execution.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();
    private static final String NAME = "GroupController";

    private GroupRepository groups;
    private UserRepository users;
    private TicketRepository tickets;
    private SystemState state;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(GroupRepository groups, UserRepository users, TicketRepository tickets, SystemState state) {
        this.groups = groups;
        this.users = users;
        this.tickets = tickets;
        this.state = state;
    }

    public Group create(String id, String name) {
        GroupRepository repository = Checks.configured(groups, NAME);
        Checks.requireText(id, "Group identifier");
        Checks.requireText(name, "Group name");
        Checks.require(repository.findById(id).isEmpty(),
                () -> new DuplicateException("A group with identifier " + id + " already exists"));
        Group group = new Group(id, name);
        repository.add(group);
        return group;
    }

    public void addMember(String groupId, String email) {
        Group group = find(groupId);
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        requireModifiable(group);
        Checks.require(!group.contains(user),
                () -> new DuplicateException("User " + email + " already belongs to group " + groupId));
        group.add(user);
        groups.save();
    }

    public void removeMember(String groupId, String email) {
        Group group = find(groupId);
        User user = users.findByEmail(email)
                .filter(group::contains)
                .orElseThrow(() -> new NotFoundException("User " + email + " is not a member of group " + groupId));
        requireModifiable(group);
        group.remove(user);
        groups.save();
    }

    public void delete(String groupId) {
        Group group = find(groupId);
        Checks.require(!isLocked(group), () -> new RuleViolationException(
                "Group " + groupId + " has active group tickets and cannot be deleted"));
        for (GroupTicket historical : tickets.findByGroup(group)) {
            historical.detachGroup();
        }
        groups.remove(group);
        tickets.save();
    }

    public boolean isLocked(Group group) {
        return tickets.hasActiveGroupTicket(group, state.getCurrentDate());
    }

    public Group find(String groupId) {
        return Checks.configured(groups, NAME).findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
    }

    private void requireModifiable(Group group) {
        Checks.require(!isLocked(group), () -> new RuleViolationException(
                "Group " + group.getId() + " has active group tickets and cannot be modified"));
    }
}
