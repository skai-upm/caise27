package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.IndividualTicket;

/**
 * TICKET CREATE;&lt;identifier&gt;;&lt;email&gt;;&lt;date&gt;.
 */
public class TicketCreateCommand extends ControllerCommand {

    public TicketCreateCommand() {
        super("TICKET CREATE;<identifier>;<email>;<date>", 3);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        IndividualTicket ticket = controller.createIndividualTicket(fields[0], fields[1], fields[2]);
        return "TICKET CREATED. PRICE: " + ticket.getPrice();
    }
}
