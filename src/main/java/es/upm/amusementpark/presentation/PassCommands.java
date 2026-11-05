package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.Pass;
import es.upm.amusementpark.business.SpecialPass;
import es.upm.amusementpark.business.SpeedPass;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * {@code SPEEDPASS CREATE/DELETE} and {@code SPECIALPASS CREATE/DELETE} commands.
 */
public final class PassCommands {

    private PassCommands() {
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        processor.register("SPEEDPASS CREATE", line -> {
            String[] f = CommandSupport.fields(line, 4,
                    "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
            int quantity;
            if ("1".equals(f[3])) {
                quantity = 1;
            } else if ("10".equals(f[3])) {
                quantity = 10;
            } else {
                throw new InvalidCommandException("Quantity must be 1 or 10");
            }
            List<SpeedPass> created = park.buySpeedPasses(f[1], f[2], quantity);
            BigDecimal total = BigDecimal.ZERO.setScale(2);
            for (Pass pass : created) {
                total = total.add(pass.getPrice());
            }
            out.println("SPEEDPASS PURCHASED: " + created.size() + " pass(es). TOTAL PRICE: " + total.toPlainString());
            return true;
        });
        processor.register("SPECIALPASS CREATE", line -> {
            String[] f = CommandSupport.fields(line, 3, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
            SpecialPass pass = park.buySpecialPass(f[1], f[2]);
            out.println("SPECIALPASS PURCHASED. PRICE: " + pass.getPrice().toPlainString());
            return true;
        });
        processor.register("SPEEDPASS DELETE", line -> {
            String[] f = CommandSupport.fields(line, 2, "SPEEDPASS DELETE;<speedPassIdentifier>");
            park.refundPass(f[1], SpeedPass.class);
            out.println("SPEEDPASS REFUNDED");
            return true;
        });
        processor.register("SPECIALPASS DELETE", line -> {
            String[] f = CommandSupport.fields(line, 2, "SPECIALPASS DELETE;<specialPassIdentifier>");
            park.refundPass(f[1], SpecialPass.class);
            out.println("SPECIALPASS REFUNDED");
            return true;
        });
    }
}
