package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.RuleViolationException;


import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Group management operations.
 */
public class GroupService {

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;

    private GroupService() {
    }

    private static final class Holder {
        private static final GroupService INSTANCE = new GroupService();
    }

    /** The single instance of this controller. */
    public static GroupService getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the repositories this controller works with (done during application initialization). */
    public GroupService inject(UserRepository users, GroupRepository groups, TicketRepository tickets) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        return this;
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
            throw new RuleViolationException("group '" + group.getIdentifier() + "' has an active group ticket");
        }
    }

    public Group createGroup(String identifier, String name) {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new InvalidDataException("group identifier and name must not be empty");
        }
        if (groups.findById(identifier).isPresent()) {
            throw new RuleViolationException("a group with identifier '" + identifier + "' already exists");
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
            throw new RuleViolationException("the user already belongs to group '" + identifier + "'");
        }
        groups.update(group);
    }

    public void removeMember(String identifier, String email) {
        Group group = findGroup(identifier);
        User user = findUser(email);
        requireUnlocked(group);
        if (!group.remove(user)) {
            throw new RuleViolationException("the user does not belong to group '" + identifier + "'");
        }
        groups.update(group);
    }

    public Group findGroup(String identifier) {
        return groups.findById(identifier)
                .orElseThrow(() -> new NotFoundException("group '" + identifier + "' not found"));
    }

    private User findUser(String email) {
        return users.findByEmail(email).orElseThrow(() -> new NotFoundException("user not found"));
    }
}
