package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * ATTRACTION CREATE;Identifier;Name;Capacity.
 */
public class AttractionCreateCommand extends ControllerCommand<ParkController> {

    public AttractionCreateCommand(ParkController controller) {
        super("ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]", 3, 4, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        if (fields.length == 4) {
            controller.createAttraction(fields[0], fields[1], fields[2], fields[3]);
        } else {
            controller.createAttraction(fields[0], fields[1], fields[2]);
        }
        return "ATTRACTION CREATED";
    }
}
