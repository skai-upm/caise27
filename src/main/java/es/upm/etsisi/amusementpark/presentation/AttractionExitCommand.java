package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * ATTRACTION EXIT;AttractionIdentifier;Email.
 */
public class AttractionExitCommand extends ControllerCommand {

    public AttractionExitCommand() {
        super("ATTRACTION EXIT;<AttractionIdentifier>;<Email>", 2);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.exitAttraction(fields[0], fields[1]);
        return "ATTRACTION EXITED";
    }
}
