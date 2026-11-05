package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Group management operations.
 */
public class GroupService {

    private final UserRepository users;
    private final GroupRepository groups;

    public GroupService(UserRepository users, GroupRepository groups) {
        this.users = users;
        this.groups = groups;
    }

    public Group createGroup(String identifier, String name) {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new OperationException("group identifier and name must not be empty");
        }
        if (groups.findById(identifier).isPresent()) {
            throw new OperationException("a group with identifier '" + identifier + "' already exists");
        }
        Group group = new Group(identifier, name);
        groups.add(group);
        return group;
    }

    public void addMember(String identifier, String email) {
        Group group = findGroup(identifier);
        User user = findUser(email);
        if (!group.add(user)) {
            throw new OperationException("the user already belongs to group '" + identifier + "'");
        }
        groups.update(group);
    }

    public void removeMember(String identifier, String email) {
        Group group = findGroup(identifier);
        User user = findUser(email);
        if (!group.remove(user)) {
            throw new OperationException("the user does not belong to group '" + identifier + "'");
        }
        groups.update(group);
    }

    public Group findGroup(String identifier) {
        return groups.findById(identifier)
                .orElseThrow(() -> new OperationException("group '" + identifier + "' not found"));
    }

    private User findUser(String email) {
        return users.findByEmail(email).orElseThrow(() -> new OperationException("user not found"));
    }
}
