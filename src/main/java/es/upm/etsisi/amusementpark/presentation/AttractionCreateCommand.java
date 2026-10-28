package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * ATTRACTION CREATE;Identifier;Name;Capacity.
 */
public class AttractionCreateCommand extends ControllerCommand<ParkController> {

    public AttractionCreateCommand(ParkController controller) {
        super("ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>", 3, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.createAttraction(fields[0], fields[1], fields[2]);
        return "ATTRACTION CREATED";
    }
}
