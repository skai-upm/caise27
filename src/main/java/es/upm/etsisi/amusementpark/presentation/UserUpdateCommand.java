package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * USER UPDATE;Email;Password;Field;NewValue.
 */
public class UserUpdateCommand extends ControllerCommand<ParkController> {

    public UserUpdateCommand(ParkController controller) {
        super("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>", 4, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.updateUser(fields[0], fields[1], fields[2], fields[3]);
        return "USER UPDATED";
    }
}
