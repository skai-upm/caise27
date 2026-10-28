package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * SPECIALPASS DELETE;&lt;specialPassIdentifier&gt;.
 */
public class SpecialPassDeleteCommand extends ControllerCommand<ParkController> {

    public SpecialPassDeleteCommand(ParkController controller) {
        super("SPECIALPASS DELETE;<specialPassIdentifier>", 1, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.refundSpecialPass(fields[0]);
        return "SPECIALPASS DELETED";
    }
}
