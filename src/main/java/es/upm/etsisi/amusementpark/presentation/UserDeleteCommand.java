package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * USER DELETE;Email;Password.
 */
public class UserDeleteCommand extends ControllerCommand<ParkController> {

    public UserDeleteCommand(ParkController controller) {
        super("USER DELETE;<Email>;<Password>", 2, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.deleteUser(fields[0], fields[1]);
        return "USER DELETED";
    }
}
