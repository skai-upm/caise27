package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.GroupOperationException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.User;

/**
 * Group management operations on the park's groups and users.
 */
public class GroupService {

    private final ParkController park = ParkController.getInstance();

    public Group createGroup(String identifier, String name) throws GroupOperationException {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new GroupOperationException("The group identifier and name cannot be empty.");
        }
        if (park.getGroups().findById(identifier).isPresent()) {
            throw new GroupOperationException("A group with identifier " + identifier + " already exists.");
        }
        Group group = new Group(identifier, name);
        park.getGroups().save(group);
        return group;
    }

    public void addUser(String identifier, String email) throws GroupOperationException {
        Group group = findGroup(identifier);
        User user = findUser(email);
        checkModifiable(group);
        if (!group.addMember(user)) {
            throw new GroupOperationException("User " + email + " already belongs to group " + identifier + ".");
        }
        park.getGroups().save(group);
    }

    public void removeUser(String identifier, String email) throws GroupOperationException {
        Group group = findGroup(identifier);
        User user = findUser(email);
        checkModifiable(group);
        if (!group.removeMember(user)) {
            throw new GroupOperationException("User " + email + " does not belong to group " + identifier + ".");
        }
        park.getGroups().save(group);
    }

    public void deleteGroup(String identifier) throws GroupOperationException {
        Group group = findGroup(identifier);
        if (park.hasActiveGroupTicket(group)) {
            throw new GroupOperationException("Group " + identifier
                    + " has a group ticket for the current date or later and cannot be deleted.");
        }
        for (GroupTicket historical : park.groupTicketsOf(group)) {
            historical.clearGroup();
        }
        park.getGroups().delete(identifier);
    }

    private void checkModifiable(Group group) throws GroupOperationException {
        if (park.hasActiveGroupTicket(group)) {
            throw new GroupOperationException("Group " + group.getIdentifier()
                    + " has a group ticket for the current date or later and cannot be modified.");
        }
    }

    private Group findGroup(String identifier) throws GroupOperationException {
        return park.getGroups().findById(identifier)
                .orElseThrow(() -> new GroupOperationException("No group with identifier " + identifier + " exists."));
    }

    private User findUser(String email) throws GroupOperationException {
        return park.getUsers().findByEmail(email)
                .orElseThrow(() -> new GroupOperationException("No user with email " + email + " exists."));
    }
}
