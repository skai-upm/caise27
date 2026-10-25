package es.upm.parque.presentation;

import java.math.BigDecimal;
import java.util.List;

import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpeedPass;

/**
 * SPEEDPASS and SPECIALPASS commands.
 */
public class PassCommands implements CommandSet {

    private final PassController passes;

    public PassCommands(PassController passes) {
        this.passes = passes;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("SPEEDPASS CREATE", "IdEntrada;Cantidad",
                "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price.",
                p -> {
                    List<SpeedPass> bought = passes.buySpeedPasses(p[0], p[1]);
                    BigDecimal total = bought.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return "OK SPEEDPASS CREATED -> " + total.toPlainString() + " "
                            + bought.stream().map(Pass::getId).toList();
                });
        registry.register("SPEEDPASS DELETE", "IdSpeedPass",
                "Refunds an unused SpeedPass before the date of its ticket.",
                p -> "OK SPEEDPASS DELETED -> " + passes.refundSpeedPass(p[0]).getPrice().toPlainString());
        registry.register("SPECIALPASS CREATE", "IdEntrada",
                "Buys the Special Pass of an individual ticket, at the ticket price (one per ticket).",
                p -> "OK SPECIALPASS CREATED -> " + passes.buySpecialPass(p[0]).getPrice().toPlainString());
        registry.register("SPECIALPASS DELETE", "IdEntrada",
                "Refunds the unused Special Pass of a ticket before the ticket date.",
                p -> "OK SPECIALPASS DELETED -> " + passes.refundSpecialPass(p[0]).getPrice().toPlainString());
    }
}
