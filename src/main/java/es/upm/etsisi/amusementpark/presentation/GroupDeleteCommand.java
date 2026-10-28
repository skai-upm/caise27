package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * GROUP DELETE;Identifier.
 */
public class GroupDeleteCommand extends ControllerCommand<ParkController> {

    public GroupDeleteCommand(ParkController controller) {
        super("GROUP DELETE;<Identifier>", 1, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.deleteGroup(fields[0]);
        return "GROUP DELETED";
    }
}
