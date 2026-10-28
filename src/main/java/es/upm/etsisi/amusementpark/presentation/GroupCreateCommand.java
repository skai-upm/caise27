package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * GROUP CREATE;Identifier;Name.
 */
public class GroupCreateCommand extends ControllerCommand<ParkController> {

    public GroupCreateCommand(ParkController controller) {
        super("GROUP CREATE;<Identifier>;<Name>", 2, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.createGroup(fields[0], fields[1]);
        return "GROUP CREATED";
    }
}
