package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;

/**
 * Business rules for user groups.
 */
public class GroupController {

    private final ParkController park;

    public GroupController(ParkController park) {
        this.park = park;
    }

    public Group find(String id) throws NotFoundException {
        Group group = park.getGroups().findById(id);
        if (group == null) {
            throw new NotFoundException("Group not found");
        }
        return group;
    }

    public void create(String id, String name) throws ParkException {
        if (park.getGroups().exists(id)) {
            throw new DuplicateException("Group already exists");
        }
        park.getGroups().add(new Group(id, name));
    }

    public void addMember(String id, String email) throws ParkException {
        Group group = modifiable(id);
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!group.addMember(user)) {
            throw new DuplicateException("User already in group");
        }
        park.getGroups().save();
    }

    public void removeMember(String id, String email) throws ParkException {
        Group group = modifiable(id);
        User user = park.getUsers().findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            throw new NotFoundException("User is not a member of the group");
        }
        park.getGroups().save();
    }

    public void delete(String id) throws ParkException {
        Group group = find(id);
        if (park.isGroupLocked(group)) {
            throw new OperationNotAllowedException("Group has active group tickets and cannot be deleted");
        }
        park.deleteGroup(group);
    }

    private Group modifiable(String id) throws ParkException {
        Group group = find(id);
        if (park.isGroupLocked(group)) {
            throw new OperationNotAllowedException("Group has active group tickets and cannot be modified");
        }
        return group;
    }
}
