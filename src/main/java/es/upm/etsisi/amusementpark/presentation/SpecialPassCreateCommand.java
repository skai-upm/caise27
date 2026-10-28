package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * SPECIALPASS CREATE;&lt;identifier&gt;;&lt;individualTicket&gt;.
 */
public class SpecialPassCreateCommand extends ControllerCommand<ParkController> {

    public SpecialPassCreateCommand(ParkController controller) {
        super("SPECIALPASS CREATE;<identifier>;<individualTicket>", 2, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        return "SPECIALPASS CREATED. PRICE: " + controller.buySpecialPass(fields[0], fields[1]).getPrice();
    }
}
