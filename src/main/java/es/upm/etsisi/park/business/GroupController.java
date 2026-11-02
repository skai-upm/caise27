package es.upm.etsisi.park.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Group lifecycle and membership, subject to the lock derived from active group tickets.
 */
public final class GroupController {

    private static final GroupController INSTANCE = new GroupController();

    private Park park;

    private GroupController() {
    }

    public static GroupController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    public Group create(String id, String name) {
        Park.requireNotBlank(id, name);
        Map<String, Group> groups = park.groups();
        if (groups.containsKey(id)) {
            throw new ConflictException("Group already exists: " + id);
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        park.commit(() -> groups.remove(id));
        return group;
    }

    public void add(String groupId, String email) {
        Group group = requireModifiable(groupId);
        User user = park.findUser(email);
        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        if (group.contains(user)) {
            throw new ConflictException("User already in group: " + email);
        }
        group.add(user);
        park.commit(() -> group.remove(user));
    }

    public void remove(String groupId, String email) {
        Group group = requireModifiable(groupId);
        User member = group.findMember(email);
        if (member == null) {
            throw new NotFoundException("User not in group: " + email);
        }
        int index = group.remove(member);
        park.commit(() -> group.add(index, member));
    }

    public void delete(String groupId) {
        Group group = requireModifiable(groupId);
        Map<String, Group> groups = park.groups();
        Map<String, Group> before = new LinkedHashMap<>(groups);
        List<GroupTicket> historical = new ArrayList<>();
        for (Ticket t : park.tickets().values()) {
            if (t instanceof GroupTicket g && g.getGroup() == group) {
                historical.add(g);
                g.clearGroup();
            }
        }
        groups.remove(groupId);
        park.commit(() -> {
            Park.restore(groups, before);
            historical.forEach(g -> g.restoreGroup(group));
        });
    }

    private Group requireModifiable(String groupId) {
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("Group not found: " + groupId);
        }
        if (park.isGroupLocked(group)) {
            throw new RuleViolationException("Group is locked by an active group ticket: " + groupId);
        }
        return group;
    }
}
