package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.DuplicateException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.User;

/**
 * Group management (Singleton). A group is locked while it has an active group ticket.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private ParkController park;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController park) {
        this.park = park;
    }

    public void create(String id, String name) {
        if (id.isBlank()) {
            throw new ValidationException("Group identifier cannot be empty");
        }
        if (park.groups().findById(id).isPresent()) {
            throw new DuplicateException("Group already exists: " + id);
        }
        park.groups().add(new Group(id, name));
    }

    public void addUser(String groupId, String email) {
        Group group = findUnlocked(groupId);
        User user = findUser(email);
        if (!group.add(user)) {
            throw new DuplicateException("User already in group: " + email);
        }
        park.groups().save();
    }

    public void removeUser(String groupId, String email) {
        Group group = findUnlocked(groupId);
        User user = findUser(email);
        if (!group.remove(user)) {
            throw new NotFoundException("User not in group: " + email);
        }
        park.groups().save();
    }

    public void delete(String groupId) {
        Group group = findUnlocked(groupId);
        park.tickets().findByGroup(group).forEach(GroupTicket::detachGroup);
        park.groups().remove(group);
        park.tickets().save();
    }

    public boolean isLocked(Group group) {
        return park.tickets().hasActiveGroupTicket(group, park.today());
    }

    public Group find(String groupId) {
        return park.groups().findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
    }

    private Group findUnlocked(String groupId) {
        Group group = find(groupId);
        if (isLocked(group)) {
            throw new OperationNotAllowedException("Group " + groupId + " is locked by an active group ticket");
        }
        return group;
    }

    private User findUser(String email) {
        return park.users().findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }
}
