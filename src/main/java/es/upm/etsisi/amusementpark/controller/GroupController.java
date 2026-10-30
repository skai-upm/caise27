package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.User;

import static es.upm.etsisi.amusementpark.controller.UserController.requireValid;

/**
 * Group operations. A group is locked while it has an active group ticket.
 */
public class GroupController {

    private final ParkController park;

    public GroupController(ParkController park) {
        this.park = park;
    }

    public void create(String id, String name) {
        requireValid(!id.isBlank(), "Group identifier cannot be empty");
        requireValid(park.groups().findById(id).isEmpty(), "Group already exists: " + id);
        park.groups().add(new Group(id, name));
    }

    public void addUser(String groupId, String email) {
        Group group = findUnlocked(groupId);
        User user = findUser(email);
        requireValid(group.add(user), "User already in group: " + email);
        park.groups().save();
    }

    public void removeUser(String groupId, String email) {
        Group group = findUnlocked(groupId);
        User user = findUser(email);
        requireValid(group.remove(user), "User not in group: " + email);
        park.groups().save();
    }

    public void delete(String groupId) {
        Group group = findUnlocked(groupId);
        park.tickets().findByGroup(group).forEach(GroupTicket::detachGroup);
        park.groups().remove(group);
        park.tickets().save();
    }

    public boolean isLocked(Group group) {
        return park.tickets().hasActiveGroupTicket(group, park.today());
    }

    Group find(String groupId) {
        return park.groups().findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    private Group findUnlocked(String groupId) {
        Group group = find(groupId);
        requireValid(!isLocked(group), "Group " + groupId + " is locked by an active group ticket");
        return group;
    }

    private User findUser(String email) {
        return park.users().findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }
}
