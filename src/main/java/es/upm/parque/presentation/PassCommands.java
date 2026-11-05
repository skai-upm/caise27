package es.upm.parque.presentation;

import java.math.BigDecimal;
import java.util.List;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.PassManager;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;

/**
 * Parses the SPEEDPASS and SPECIALPASS commands and formats their responses; the rules live in {@link PassManager}.
 */
class PassCommands {

    static final String SPEEDPASS_CREATE = "SPEEDPASS CREATE";
    static final String SPECIALPASS_CREATE = "SPECIALPASS CREATE";
    static final String SPEEDPASS_DELETE = "SPEEDPASS DELETE";
    static final String SPECIALPASS_DELETE = "SPECIALPASS DELETE";
    private static final String EUR = " EUR";

    private final PassManager passes;

    PassCommands(PassManager passes) {
        this.passes = passes;
    }

    String createSpeedPasses(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 4,
                "SPEEDPASS CREATE expects: SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
        List<SpeedPass> created = passes.buySpeedPasses(e[1], e[2], e[3]);
        BigDecimal unit = created.get(0).getPrice();
        BigDecimal total = created.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        String ids = created.size() == 1 ? created.get(0).getId()
                : created.get(0).getId() + " to " + created.get(created.size() - 1).getId();
        return "OK: " + created.size() + (created.size() == 1 ? " SpeedPass" : " SpeedPasses") + " created for ticket "
                + e[2] + " (" + ids + "), unit price " + unit.toPlainString() + EUR + ", total "
                + total.toPlainString() + EUR;
    }

    String createSpecialPass(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 3, "SPECIALPASS CREATE expects: SPECIALPASS CREATE;<identifier>;<individualTicket>");
        SpecialPass pass = passes.buySpecialPass(e[1], e[2]);
        return "OK: Special Pass " + pass.getId() + " created for ticket " + e[2] + ", price "
                + pass.getPrice().toPlainString() + EUR;
    }

    String refundSpeedPass(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "SPEEDPASS DELETE expects: SPEEDPASS DELETE;<speedPassIdentifier>");
        return refunded(passes.refund(e[1], SpeedPass.class, "SpeedPass"));
    }

    String refundSpecialPass(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "SPECIALPASS DELETE expects: SPECIALPASS DELETE;<specialPassIdentifier>");
        return refunded(passes.refund(e[1], SpecialPass.class, "Special Pass"));
    }

    private static String refunded(Pass pass) {
        return "OK: " + pass.typeName() + " " + pass.getId() + " refunded, " + pass.getPrice().toPlainString() + EUR;
    }
}
