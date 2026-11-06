package es.upm.park.service;

import java.util.List;
import java.util.function.Predicate;

import es.upm.park.model.Group;
import es.upm.park.model.User;
import es.upm.park.repository.GroupRepository;
import es.upm.park.repository.UserRepository;

/**
 * Operations on user groups. A group is locked while it has an active group
 * ticket; that condition is evaluated on demand through {@code activeTicket}.
 */
public class GroupService {

    private final GroupRepository groups;
    private final UserRepository users;
    private final Predicate<Group> activeTicket;

    public GroupService(GroupRepository groups, UserRepository users, Predicate<Group> activeTicket) {
        this.groups = groups;
        this.users = users;
        this.activeTicket = activeTicket;
    }

    public Group create(String id, String name) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new GroupException("group identifier and name are required");
        }
        if (groups.findById(id).isPresent()) {
            throw new GroupException("group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.add(group);
        return group;
    }

    public void addUser(String id, String email) {
        Group group = modifiableGroup(id);
        User user = user(email);
        if (!group.addMember(user.getDni())) {
            throw new GroupException("user " + email + " already in group " + id);
        }
    }

    public void removeUser(String id, String email) {
        Group group = modifiableGroup(id);
        User user = user(email);
        if (!group.removeMember(user.getDni())) {
            throw new GroupException("user " + email + " not in group " + id);
        }
    }

    public void delete(String id) {
        groups.remove(modifiableGroup(id));
    }

    /** Groups the user currently belongs to, found by traversing groups. */
    public List<Group> groupsOf(User user) {
        return groups.findAll().stream().filter(g -> g.contains(user.getDni())).toList();
    }

    /** Whether the group currently has an active group ticket. */
    public boolean isLocked(Group group) {
        return activeTicket.test(group);
    }

    public Group find(String id) {
        return groups.findById(id).orElseThrow(() -> new GroupException("no group " + id));
    }

    private Group modifiableGroup(String id) {
        Group group = find(id);
        if (isLocked(group)) {
            throw new GroupException("group " + id + " has an active group ticket");
        }
        return group;
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new UserException("no user with email " + email));
    }
}
