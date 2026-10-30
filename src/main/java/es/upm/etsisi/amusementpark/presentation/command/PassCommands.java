package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;

import java.util.List;

/**
 * SPEEDPASS and SPECIALPASS CREATE / DELETE.
 */
public class PassCommands implements CommandModule {

    private final PassController passes;

    public PassCommands(PassController passes) {
        this.passes = passes;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "Buys 1 or 10 SpeedPasses (20% of the ticket price each) named <purchaseIdentifier>:<n>.",
                p -> {
                    List<SpeedPass> created = passes.createSpeedPasses(p[1], p[2], p[3]);
                    return "SPEEDPASS CREATED - " + created.size() + " pass(es) - TOTAL PRICE: "
                            + PassController.total(created) + " EUR";
                });
        registry.register("SPEEDPASS DELETE;<speedPassIdentifier>",
                "Refunds an unconsumed SpeedPass of a ticket dated after today.",
                p -> {
                    passes.deleteSpeedPass(p[1]);
                    return "SPEEDPASS DELETED";
                });
        registry.register("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "Buys the single Special Pass of an individual ticket, priced as the ticket.",
                p -> "SPECIALPASS CREATED - PRICE: " + passes.createSpecialPass(p[1], p[2]).getPrice() + " EUR");
        registry.register("SPECIALPASS DELETE;<specialPassIdentifier>",
                "Refunds an unconsumed Special Pass of a ticket dated after today.",
                p -> {
                    passes.deleteSpecialPass(p[1]);
                    return "SPECIALPASS DELETED";
                });
    }
}
