package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.exception.ParkException;

/**
 * GROUP CREATE / ADD / REMOVE / DELETE commands.
 */
class GroupCommands {

    private final GroupController groups;

    GroupCommands(GroupController groups) {
        this.groups = groups;
    }

    String create(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "GROUP CREATE;Identifier;Name");
        groups.create(in.get(1), in.get(2));
        return "GROUP CREATED";
    }

    String add(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "GROUP ADD;Identifier;Email");
        groups.addMember(in.get(1), in.get(2));
        return "GROUP USER ADDED";
    }

    String remove(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "GROUP REMOVE;Identifier;Email");
        groups.removeMember(in.get(1), in.get(2));
        return "GROUP USER REMOVED";
    }

    String delete(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "GROUP DELETE;Identifier");
        groups.delete(in.get(1));
        return "GROUP DELETED";
    }
}
