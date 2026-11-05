package es.upm.amusementpark.service;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Group management operations.
 */
public class GroupService {

    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;

    public GroupService(UserRepository users, GroupRepository groups) {
        this(users, groups, new TicketRepository());
    }

    public GroupService(UserRepository users, GroupRepository groups, TicketRepository tickets) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
    }

    public void deleteGroup(String identifier) {
        Group group = findGroup(identifier);
        requireUnlocked(group);
        tickets.groupTicketsOf(group).forEach(t -> t.detachGroup());
        groups.remove(group);
        tickets.update();
    }

    private void requireUnlocked(Group group) {
        if (tickets.isLocked(group, Park.getInstance().getCurrentDate())) {
            throw new OperationException("group '" + group.getIdentifier() + "' has an active group ticket");
        }
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
        requireUnlocked(group);
        if (!group.add(user)) {
            throw new OperationException("the user already belongs to group '" + identifier + "'");
        }
        groups.update(group);
    }

    public void removeMember(String identifier, String email) {
        Group group = findGroup(identifier);
        User user = findUser(email);
        requireUnlocked(group);
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
