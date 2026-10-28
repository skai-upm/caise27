package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * GROUP REMOVE;Identifier;Email.
 */
public class GroupRemoveCommand extends ControllerCommand<ParkController> {

    public GroupRemoveCommand(ParkController controller) {
        super("GROUP REMOVE;<Identifier>;<Email>", 2, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.removeUserFromGroup(fields[0], fields[1]);
        return "USER REMOVED FROM GROUP";
    }
}
