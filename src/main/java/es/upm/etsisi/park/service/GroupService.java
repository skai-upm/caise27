package es.upm.etsisi.park.service;

import es.upm.etsisi.park.core.SystemState;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.repository.GroupRepository;
import es.upm.etsisi.park.repository.TicketRepository;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.validation.UserValidator;

/**
 * GROUP CREATE / ADD / REMOVE / DELETE. A group is locked while it has an active group
 * ticket; the lock is always derived from the tickets and the current date.
 */
public class GroupService {

    private final GroupRepository groups;
    private final UserRepository users;
    private final TicketRepository tickets;
    private final SystemState state;

    public GroupService(GroupRepository groups, UserRepository users, TicketRepository tickets, SystemState state) {
        this.groups = groups;
        this.users = users;
        this.tickets = tickets;
        this.state = state;
    }

    public Group create(String id, String name) {
        Checks.require(UserValidator.isNotBlank(id), "Group identifier must not be empty");
        Checks.require(UserValidator.isNotBlank(name), "Group name must not be empty");
        Checks.require(groups.findById(id).isEmpty(), "A group with identifier " + id + " already exists");
        Group group = new Group(id, name);
        groups.add(group);
        return group;
    }

    public void addMember(String groupId, String email) {
        Group group = find(groupId);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("User not found: " + email));
        requireModifiable(group);
        Checks.require(!group.contains(user), "User " + email + " already belongs to group " + groupId);
        group.add(user);
        groups.save();
    }

    public void removeMember(String groupId, String email) {
        Group group = find(groupId);
        User user = users.findByEmail(email)
                .filter(group::contains)
                .orElseThrow(() -> new UserOperationException("User " + email + " is not a member of group " + groupId));
        requireModifiable(group);
        group.remove(user);
        groups.save();
    }

    public void delete(String groupId) {
        Group group = find(groupId);
        Checks.require(!isLocked(group), "Group " + groupId + " has active group tickets and cannot be deleted");
        for (GroupTicket historical : tickets.findByGroup(group)) {
            historical.detachGroup();
        }
        groups.remove(group);
        tickets.save();
    }

    public boolean isLocked(Group group) {
        return tickets.hasActiveGroupTicket(group, state.getCurrentDate());
    }

    private void requireModifiable(Group group) {
        Checks.require(!isLocked(group), "Group " + group.getId() + " has active group tickets and cannot be modified");
    }

    private Group find(String groupId) {
        return groups.findById(groupId)
                .orElseThrow(() -> new UserOperationException("Group not found: " + groupId));
    }
}
