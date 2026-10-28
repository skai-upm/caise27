package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * ATTRACTION CREATE;Identifier;Name;Capacity.
 */
public class AttractionCreateCommand extends ControllerCommand {

    public AttractionCreateCommand() {
        super("ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>", 3);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.createAttraction(fields[0], fields[1], fields[2]);
        return "ATTRACTION CREATED";
    }
}
