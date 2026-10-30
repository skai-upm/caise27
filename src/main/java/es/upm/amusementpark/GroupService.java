package es.upm.amusementpark;

/**
 * Group operations. Membership changes and deletion are refused while the group is locked by an active
 * group ticket.
 */
public class GroupService {

    private final Park park = Park.getInstance();

    public Group create(String id, String name) {
        if (park.getGroups().findById(id) != null) {
            throw new IllegalArgumentException("a group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        park.getGroups().add(group);
        return group;
    }

    public void add(String groupId, String email) {
        Group group = modifiableGroup(groupId);
        group.addMember(existingUser(email));
        park.getGroups().save();
    }

    public void remove(String groupId, String email) {
        Group group = modifiableGroup(groupId);
        group.removeMember(existingUser(email));
        park.getGroups().save();
    }

    /**
     * Deletes the group. Its historical group tickets are kept with a null group reference.
     */
    public void delete(String groupId) {
        Group group = modifiableGroup(groupId);
        park.getTickets().detachGroup(group);
        park.getGroups().remove(group);
    }

    private Group modifiableGroup(String groupId) {
        Group group = existingGroup(groupId);
        if (park.isLocked(group)) {
            throw new IllegalArgumentException("group " + groupId + " is locked by an active group ticket");
        }
        return group;
    }

    Group existingGroup(String groupId) {
        Group group = park.getGroups().findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("no group with identifier " + groupId);
        }
        return group;
    }

    private User existingUser(String email) {
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        return user;
    }
}
