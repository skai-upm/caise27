package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

/**
 * GROUP ADD;Identifier;Email.
 */
public class GroupAddCommand extends ControllerCommand {

    public GroupAddCommand() {
        super("GROUP ADD;<Identifier>;<Email>", 2);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.addUserToGroup(fields[0], fields[1]);
        return "USER ADDED TO GROUP";
    }
}
