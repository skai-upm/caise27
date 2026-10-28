package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

/**
 * ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier.
 */
public class AttractionEnterCommand extends ControllerCommand {

    public AttractionEnterCommand() {
        super("ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>", 2);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        int entered = controller.enterAttraction(fields[0], fields[1]);
        return "ATTRACTION ENTERED: " + entered + " user(s) in " + fields[0];
    }
}
