package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

/**
 * GROUP CREATE;Identifier;Name.
 */
public class GroupCreateCommand extends ControllerCommand {

    public GroupCreateCommand() {
        super("GROUP CREATE;<Identifier>;<Name>", 2);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.createGroup(fields[0], fields[1]);
        return "GROUP CREATED";
    }
}
