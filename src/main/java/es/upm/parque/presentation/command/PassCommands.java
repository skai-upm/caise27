package es.upm.parque.presentation.command;

import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassKind;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.presentation.CommandRegistry;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SPEEDPASS and SPECIALPASS commands.
 */
public final class PassCommands {

    private PassCommands() {
    }

    public static void register(CommandRegistry registry, PassController passes) {
        registry.register("SPEEDPASS CREATE;IdEntrada;Cantidad",
                "Buys 1 or 10 SpeedPasses for an individual ticket; each costs 20% of the ticket price.",
                p -> {
                    List<SpeedPass> bought = passes.buySpeedPasses(p[1], p[2]);
                    BigDecimal total = bought.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return "OK SPEEDPASS CREATED -> " + total.toPlainString() + " ("
                            + bought.stream().map(Pass::getId).collect(Collectors.joining(", ")) + ")";
                });
        registry.register("SPEEDPASS DELETE;IdPase",
                "Refunds an unused SpeedPass whose ticket is dated after today.",
                p -> "OK SPEEDPASS DELETED -> " + passes.refund(p[1], PassKind.SPEED).getPrice().toPlainString());
        registry.register("SPECIALPASS CREATE;IdEntrada",
                "Buys the Special Pass of an individual ticket (one per ticket); it costs the ticket price.",
                p -> {
                    SpecialPass pass = passes.buySpecialPass(p[1]);
                    return "OK SPECIALPASS CREATED -> " + pass.getPrice().toPlainString() + " (" + pass.getId() + ")";
                });
        registry.register("SPECIALPASS DELETE;IdPase",
                "Refunds an unused Special Pass whose ticket is dated after today.",
                p -> "OK SPECIALPASS DELETED -> " + passes.refund(p[1], PassKind.SPECIAL).getPrice().toPlainString());
    }
}
