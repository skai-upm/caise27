package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

/**
 * GROUP REMOVE;Identifier;Email.
 */
public class GroupRemoveCommand extends ControllerCommand {

    public GroupRemoveCommand() {
        super("GROUP REMOVE;<Identifier>;<Email>", 2);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.removeUserFromGroup(fields[0], fields[1]);
        return "USER REMOVED FROM GROUP";
    }
}
