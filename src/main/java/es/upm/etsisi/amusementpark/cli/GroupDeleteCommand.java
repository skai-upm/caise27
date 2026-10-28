package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

/**
 * GROUP DELETE;Identifier.
 */
public class GroupDeleteCommand extends ControllerCommand {

    public GroupDeleteCommand() {
        super("GROUP DELETE;<Identifier>", 1);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.deleteGroup(fields[0]);
        return "GROUP DELETED";
    }
}
