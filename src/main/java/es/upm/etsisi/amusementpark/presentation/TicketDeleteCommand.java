package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * TICKET DELETE;&lt;identifier&gt;.
 */
public class TicketDeleteCommand extends ControllerCommand<ParkController> {

    public TicketDeleteCommand(ParkController controller) {
        super("TICKET DELETE;<identifier>", 1, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.deleteIndividualTicket(fields[0]);
        return "TICKET DELETED";
    }
}
