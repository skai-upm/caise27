package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * GROUPTICKET DELETE;&lt;identifier&gt;.
 */
public class GroupTicketDeleteCommand extends ControllerCommand {

    public GroupTicketDeleteCommand() {
        super("GROUPTICKET DELETE;<identifier>", 1);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.deleteGroupTicket(fields[0]);
        return "GROUPTICKET DELETED";
    }
}
