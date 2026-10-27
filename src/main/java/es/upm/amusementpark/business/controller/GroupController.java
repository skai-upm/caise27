package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;

/**
 * Business rules for user groups.
 */
public final class GroupController {

    private static GroupController instance;

    private GroupRepository groups;
    private UserRepository users;
    private ParkController park;

    private GroupController() {
    }

    public static GroupController getInstance() {
        if (instance == null) {
            instance = new GroupController();
        }
        return instance;
    }

    /** Injects the dependencies configured at application initialization. */
    public void configure(GroupRepository groups, UserRepository users, ParkController park) {
        this.groups = groups;
        this.users = users;
        this.park = park;
    }

    public Group find(String id) throws NotFoundException {
        Group group = groups.findById(id);
        if (group == null) {
            throw new NotFoundException("Group not found");
        }
        return group;
    }

    public void create(String id, String name) throws ParkException {
        if (groups.exists(id)) {
            throw new DuplicateException("Group already exists");
        }
        groups.add(new Group(id, name));
    }

    public void addMember(String id, String email) throws ParkException {
        Group group = modifiable(id);
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!group.addMember(user)) {
            throw new DuplicateException("User already in group");
        }
        groups.save();
    }

    public void removeMember(String id, String email) throws ParkException {
        Group group = modifiable(id);
        User user = users.findByEmail(email);
        if (user == null || !group.removeMember(user)) {
            throw new NotFoundException("User is not a member of the group");
        }
        groups.save();
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
