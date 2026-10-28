package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;

/**
 * GROUPTICKET CREATE;&lt;identifier&gt;;&lt;group&gt;;&lt;date&gt;.
 */
public class GroupTicketCreateCommand extends ControllerCommand<ParkController> {

    public GroupTicketCreateCommand(ParkController controller) {
        super("GROUPTICKET CREATE;<identifier>;<group>;<date>", 3, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        GroupTicket ticket = controller.createGroupTicket(fields[0], fields[1], fields[2]);
        return "GROUPTICKET CREATED. TOTAL PRICE: " + ticket.getPrice();
    }
}
