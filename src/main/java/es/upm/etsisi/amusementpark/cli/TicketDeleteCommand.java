package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;

/**
 * TICKET DELETE;&lt;identifier&gt;.
 */
public class TicketDeleteCommand extends ControllerCommand {

    public TicketDeleteCommand() {
        super("TICKET DELETE;<identifier>", 1);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.deleteIndividualTicket(fields[0]);
        return "TICKET DELETED";
    }
}
