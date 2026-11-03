package es.upm.etsisi.park;

import java.util.List;
import java.util.Map;

/**
 * Group management. A group's lock is always derived from its tickets.
 */
public class GroupService {

    private final Park park;

    public GroupService(Park park) {
        this.park = park;
    }

    public void create(String id, String name) throws ValidationException {
        requireText(id, "group identifier");
        requireText(name, "group name");
        if (park.findGroup(id) != null) {
            throw new ValidationException("group " + id + " already exists");
        }
        Map<String, Group> before = park.snapshotGroups();
        park.addGroup(new Group(id, name));
        park.commit(() -> park.replaceGroups(before));
    }

    public void addMember(String groupId, String email) throws ValidationException {
        Group group = modifiableGroup(groupId);
        User user = requireUser(email);
        if (group.contains(user)) {
            throw new ValidationException("user " + email + " already belongs to group " + groupId);
        }
        group.add(user);
        park.commit(() -> group.remove(user));
    }

    public void removeMember(String groupId, String email) throws ValidationException {
        Group group = modifiableGroup(groupId);
        User user = requireUser(email);
        if (!group.contains(user)) {
            throw new ValidationException("user " + email + " does not belong to group " + groupId);
        }
        int index = group.indexOf(user);
        group.remove(user);
        park.commit(() -> group.add(index, user));
    }

    public void delete(String groupId) throws ValidationException {
        Group group = modifiableGroup(groupId);
        List<GroupTicket> historical = park.groupTicketsOf(group);
        Map<String, Group> before = park.snapshotGroups();
        Map<String, Group> after = park.snapshotGroups();
        after.remove(groupId);
        park.replaceGroups(after);
        historical.forEach(GroupTicket::clearGroup);
        park.commit(() -> {
            park.replaceGroups(before);
            historical.forEach(t -> t.restoreGroup(group));
        });
    }

    private Group modifiableGroup(String groupId) throws ValidationException {
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new ValidationException("no group with identifier " + groupId);
        }
        if (park.isGroupLocked(group)) {
            throw new ValidationException("group " + groupId + " is locked by an active group ticket");
        }
        return group;
    }

    private User requireUser(String email) throws ValidationException {
        User user = park.findUser(email);
        if (user == null) {
            throw new ValidationException("no user with email " + email);
        }
        return user;
    }

    static void requireText(String value, String what) throws ValidationException {
        if (value.isBlank()) {
            throw new ValidationException(what + " cannot be empty");
        }
    }
}
