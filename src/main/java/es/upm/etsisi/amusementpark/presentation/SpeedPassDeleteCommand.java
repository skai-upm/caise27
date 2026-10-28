package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * SPEEDPASS DELETE;&lt;speedPassIdentifier&gt;.
 */
public class SpeedPassDeleteCommand extends ControllerCommand<ParkController> {

    public SpeedPassDeleteCommand(ParkController controller) {
        super("SPEEDPASS DELETE;<speedPassIdentifier>", 1, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        controller.refundSpeedPass(fields[0]);
        return "SPEEDPASS DELETED";
    }
}
