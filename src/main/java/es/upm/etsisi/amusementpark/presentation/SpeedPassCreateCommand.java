package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;

import java.math.BigDecimal;
import java.util.List;

/**
 * SPEEDPASS CREATE;&lt;purchaseIdentifier&gt;;&lt;individualTicket&gt;;&lt;quantity&gt;.
 */
public class SpeedPassCreateCommand extends ControllerCommand<ParkController> {

    public SpeedPassCreateCommand(ParkController controller) {
        super("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>", 3, controller);
    }

    @Override
    protected String run(ParkController controller, String[] fields) {
        List<SpeedPass> created = controller.buySpeedPasses(fields[0], fields[1], fields[2]);
        BigDecimal total = created.stream().map(SpeedPass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return "SPEEDPASS CREATED: " + created.size() + " pass(es). TOTAL PRICE: " + total;
    }
}
