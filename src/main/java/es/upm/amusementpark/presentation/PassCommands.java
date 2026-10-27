package es.upm.amusementpark.presentation;

import java.math.BigDecimal;

import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.SpecialPass;

/**
 * SPEEDPASS and SPECIALPASS commands.
 */
class PassCommands {

    private final PassController passes;

    PassCommands(PassController passes) {
        this.passes = passes;
    }

    String createSpeed(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(3, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
        int quantity = in.number(3, "Quantity must be 1 or 10");
        BigDecimal total = passes.buySpeedPasses(in.get(1), in.get(2), quantity);
        return "SPEEDPASS CREATED - Total price: " + total.toPlainString() + " EUR";
    }

    String deleteSpeed(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "SPEEDPASS DELETE;<speedPassIdentifier>");
        passes.refundSpeedPass(in.get(1));
        return "SPEEDPASS DELETED";
    }

    String createSpecial(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
        SpecialPass pass = passes.buySpecialPass(in.get(1), in.get(2));
        return "SPECIALPASS CREATED - Price: " + pass.getPrice().toPlainString() + " EUR";
    }

    String deleteSpecial(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "SPECIALPASS DELETE;<specialPassIdentifier>");
        passes.refundSpecialPass(in.get(1));
        return "SPECIALPASS DELETED";
    }
}
