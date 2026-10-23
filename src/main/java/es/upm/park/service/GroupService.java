package es.upm.park.service;

import es.upm.park.model.Group;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.validation.UserValidator;

/**
 * Group operations. A group can only change while it has no active group
 * ticket; this is always derived from the park's tickets.
 */
public class GroupService {

    private final Park park;

    public GroupService(Park park) {
        this.park = park;
    }

    public Group create(String id, String name) throws OperationException {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new OperationException("The group identifier and name must not be empty");
        }
        if (park.findGroup(id) != null) {
            throw new OperationException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        park.addGroup(group);
        park.save();
        return group;
    }

    public void addMember(String groupId, String email) throws OperationException {
        Group group = modifiableGroup(groupId);
        User user = existingUser(email);
        if (!group.add(user)) {
            throw new OperationException("The user " + email + " already belongs to group " + groupId);
        }
        park.save();
    }

    public void removeMember(String groupId, String email) throws OperationException {
        Group group = modifiableGroup(groupId);
        User user = existingUser(email);
        if (!group.remove(user)) {
            throw new OperationException("The user " + email + " does not belong to group " + groupId);
        }
        park.save();
    }

    public void delete(String groupId) throws OperationException {
        Group group = existingGroup(groupId);
        if (park.isLocked(group)) {
            throw new OperationException("Group " + groupId + " has active group tickets and cannot be deleted");
        }
        for (Ticket ticket : park.tickets()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTicket.detachGroup();
            }
        }
        park.removeGroup(group);
        park.save();
    }

    private Group existingGroup(String groupId) throws OperationException {
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new OperationException("No group with identifier " + groupId);
        }
        return group;
    }

    private Group modifiableGroup(String groupId) throws OperationException {
        Group group = existingGroup(groupId);
        if (park.isLocked(group)) {
            throw new OperationException("Group " + groupId + " is locked by an active group ticket");
        }
        return group;
    }

    private User existingUser(String email) throws OperationException {
        User user = park.users().findByEmail(email);
        if (user == null) {
            throw new OperationException("No user with email " + email);
        }
        return user;
    }
}
