package es.upm.amusementpark.command;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.ParkController;

/**
 * GROUP CREATE / ADD / REMOVE / DELETE commands.
 */
class GroupCommands {

    private final ParkController park;

    GroupCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] parts) {
        if (parts.length != 3 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use GROUP CREATE;Identifier;Name";
        }
        if (park.getGroups().exists(parts[1])) {
            return "ERROR: Group already exists";
        }
        park.getGroups().add(new Group(parts[1], parts[2]));
        return "GROUP CREATED";
    }

    String add(String[] parts) {
        if (parts.length != 3 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use GROUP ADD;Identifier;Email";
        }
        Group group = park.getGroups().findById(parts[1]);
        if (group == null) {
            return "ERROR: Group not found";
        }
        User user = park.getUsers().findByEmail(parts[2]);
        if (user == null) {
            return "ERROR: User not found";
        }
        if (!group.addMember(user)) {
            return "ERROR: User already in group";
        }
        return "GROUP USER ADDED";
    }

    String remove(String[] parts) {
        if (parts.length != 3 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use GROUP REMOVE;Identifier;Email";
        }
        Group group = park.getGroups().findById(parts[1]);
        if (group == null) {
            return "ERROR: Group not found";
        }
        User user = park.getUsers().findByEmail(parts[2]);
        if (user == null || !group.removeMember(user)) {
            return "ERROR: User is not a member of the group";
        }
        return "GROUP USER REMOVED";
    }

    String delete(String[] parts) {
        if (parts.length != 2 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use GROUP DELETE;Identifier";
        }
        Group group = park.getGroups().findById(parts[1]);
        if (group == null) {
            return "ERROR: Group not found";
        }
        park.getGroups().remove(group);
        return "GROUP DELETED";
    }
}
