package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card.
 */
public class UserCreateCommand extends ControllerCommand<ParkController> {

    public UserCreateCommand(ParkController controller) {
        super("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", 7, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.registerUser(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
        return "USER CREATED";
    }
}
