package es.upm.etsisi.amusementpark.business.group;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.util.List;

/**
 * Singleton controller for groups and their membership.
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

    public void configure(Park parkState) {
        park = parkState;
    }

    /** A group is locked while any of its group tickets is active; this is never stored. */
    public boolean isLocked(Group group) {
        return park.tickets().stream().anyMatch(t -> t instanceof GroupTicket gt
                && gt.getGroup() == group && gt.isActiveOn(park.getCurrentDate()));
    }

    public List<Group> groupsOf(User user) {
        return park.groups().stream().filter(g -> g.hasMember(user)).toList();
    }

    public void create(String id, String name) throws BusinessException {
        if (id.isBlank() || name.isBlank()) {
            throw new InvalidDataException("group identifier and name must not be empty");
        }
        if (park.findGroup(id).isPresent()) {
            throw new OperationNotAllowedException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        park.groups().add(group);
        park.commit(() -> park.groups().remove(group));
    }

    public void addMember(String groupId, String email) throws BusinessException {
        Group group = requireModifiable(groupId);
        User user = park.requireUser(email);
        if (group.hasMember(user)) {
            throw new OperationNotAllowedException("user already belongs to group " + groupId);
        }
        group.addMember(user);
        park.commit(() -> group.removeMember(user));
    }

    public void removeMember(String groupId, String email) throws BusinessException {
        Group group = requireModifiable(groupId);
        User user = park.requireUser(email);
        int index = group.removeMember(user);
        if (index < 0) {
            throw new OperationNotAllowedException("user does not belong to group " + groupId);
        }
        park.commit(() -> group.insertMember(index, user));
    }

    /** Deletes an unlocked group; its historical group tickets keep their data without the group. */
    public void delete(String groupId) throws BusinessException {
        Group group = requireModifiable(groupId);
        int index = park.groups().indexOf(group);
        List<GroupTicket> history = park.tickets().stream()
                .filter(t -> t instanceof GroupTicket gt && gt.getGroup() == group)
                .map(GroupTicket.class::cast).toList();
        park.groups().remove(index);
        history.forEach(t -> t.setGroup(null));
        park.commit(() -> {
            park.groups().add(index, group);
            history.forEach(t -> t.setGroup(group));
        });
    }

    private Group requireModifiable(String id) throws BusinessException {
        Group group = park.requireGroup(id);
        if (isLocked(group)) {
            throw new OperationNotAllowedException("group " + id + " has an active group ticket");
        }
        return group;
    }
}
