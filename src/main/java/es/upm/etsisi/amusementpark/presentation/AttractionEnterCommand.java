package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier.
 */
public class AttractionEnterCommand extends ControllerCommand<ParkController> {

    public AttractionEnterCommand(ParkController controller) {
        super("ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>", 2, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        int entered = controller.enterAttraction(fields[0], fields[1]);
        return "ATTRACTION ENTERED: " + entered + " user(s) in " + fields[0];
    }
}
