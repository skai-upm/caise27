package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * GROUP ADD;Identifier;Email.
 */
public class GroupAddCommand extends ControllerCommand<ParkController> {

    public GroupAddCommand(ParkController controller) {
        super("GROUP ADD;<Identifier>;<Email>", 2, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.addUserToGroup(fields[0], fields[1]);
        return "USER ADDED TO GROUP";
    }
}
