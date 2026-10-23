package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.ConflictException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.business.validation.UserValidator;

/**
 * Group operations. A group can only change while it has no active group
 * ticket; this is always derived from the park's tickets. Single instance.
 */
public final class GroupController {

    private Park park;

    private GroupController() {
    }

    private static final class Holder {
        private static final GroupController INSTANCE = new GroupController();
    }

    public static GroupController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    private Park park() {
        if (park == null) {
            throw new IllegalStateException("GroupController has not been configured");
        }
        return park;
    }

    public Group create(String id, String name) throws ParkException {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ValidationException("The group identifier and name must not be empty");
        }
        if (park().findGroup(id) != null) {
            throw new ConflictException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        park().addGroup(group);
        park().save();
        return group;
    }

    public void addMember(String groupId, String email) throws ParkException {
        Group group = modifiableGroup(groupId);
        User user = existingUser(email);
        if (!group.add(user)) {
            throw new ConflictException("The user " + email + " already belongs to group " + groupId);
        }
        park().save();
    }

    public void removeMember(String groupId, String email) throws ParkException {
        Group group = modifiableGroup(groupId);
        User user = existingUser(email);
        if (!group.remove(user)) {
            throw new NotFoundException("The user " + email + " does not belong to group " + groupId);
        }
        park().save();
    }

    public void delete(String groupId) throws ParkException {
        Group group = existingGroup(groupId);
        if (park().isLocked(group)) {
            throw new BusinessRuleException("Group " + groupId + " has active group tickets and cannot be deleted");
        }
        for (Ticket ticket : park().tickets()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTicket.detachGroup();
            }
        }
        park().removeGroup(group);
        park().save();
    }

    public Group existingGroup(String groupId) throws NotFoundException {
        Group group = park().findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("No group with identifier " + groupId);
        }
        return group;
    }

    private Group modifiableGroup(String groupId) throws ParkException {
        Group group = existingGroup(groupId);
        if (park().isLocked(group)) {
            throw new BusinessRuleException("Group " + groupId + " is locked by an active group ticket");
        }
        return group;
    }

    private User existingUser(String email) throws NotFoundException {
        User user = park().findUserByEmail(email);
        if (user == null) {
            throw new NotFoundException("No user with email " + email);
        }
        return user;
    }
}
