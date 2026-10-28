package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.GroupTicket;

/**
 * GROUPTICKET CREATE;&lt;identifier&gt;;&lt;group&gt;;&lt;date&gt;.
 */
public class GroupTicketCreateCommand extends ControllerCommand {

    public GroupTicketCreateCommand() {
        super("GROUPTICKET CREATE;<identifier>;<group>;<date>", 3);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        GroupTicket ticket = controller.createGroupTicket(fields[0], fields[1], fields[2]);
        return "GROUPTICKET CREATED. TOTAL PRICE: " + ticket.getPrice();
    }
}
