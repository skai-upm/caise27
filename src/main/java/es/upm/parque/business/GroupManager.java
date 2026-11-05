package es.upm.parque.business;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.User;

/**
 * Business operations on user groups, honouring the lock derived from active group tickets.
 */
public class GroupManager {

    private static final String NO_GROUP = "no group with identifier ";

    private ParkController park;

    private GroupManager() {
    }

    private static final class Holder {
        private static final GroupManager INSTANCE = new GroupManager();
    }

    /** Returns the only instance of this controller. */
    public static GroupManager getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management configured during application initialization. */
    public void inject(ParkController park) {
        this.park = park;
    }

    private ParkController park() {
        return ParkConfiguration.requireInjected(park);
    }

    public Group create(String id, String name) throws ParkException {
        if (park().getGroups().exists(id)) {
            throw new RuleViolationException("a group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        park().getGroups().add(group);
        park().persist();
        return group;
    }

    public Group add(String id, String email) throws ParkException {
        Group group = modifiableGroup(id);
        User user = park().getUsers().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        if (!group.add(user)) {
            throw new RuleViolationException("user " + email + " already belongs to group " + id);
        }
        park().persist();
        return group;
    }

    public Group remove(String id, String email) throws ParkException {
        Group group = modifiableGroup(id);
        User user = park().getUsers().findByEmail(email);
        if (user == null || !group.remove(user)) {
            throw new NotFoundException("user " + email + " is not a member of group " + id);
        }
        park().persist();
        return group;
    }

    /** Deletes the group when it has no group ticket for today or later; historical tickets lose their group. */
    public Group delete(String id) throws ParkException {
        Group group = find(id);
        if (park().hasActiveGroupTicket(group)) {
            throw new RuleViolationException("group " + id
                    + " has a group ticket for today or a later date and cannot be deleted");
        }
        park().groupTicketsOf(group).forEach(GroupTicket::detachGroup);
        park().getGroups().remove(id);
        park().persist();
        return group;
    }

    public Group find(String id) throws NotFoundException {
        Group group = park().getGroups().findById(id);
        if (group == null) {
            throw new NotFoundException(NO_GROUP + id);
        }
        return group;
    }

    private Group modifiableGroup(String id) throws ParkException {
        Group group = find(id);
        if (park().hasActiveGroupTicket(group)) {
            throw new RuleViolationException("group " + id + " has an active group ticket and cannot be modified");
        }
        return group;
    }
}
