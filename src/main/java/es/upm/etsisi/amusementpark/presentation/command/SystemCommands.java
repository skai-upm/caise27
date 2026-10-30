package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

/**
 * SYSTEM DATE / PRICE.
 */
public class SystemCommands implements CommandModule {

    private final ParkController park;

    public SystemCommands(ParkController park) {
        this.park = park;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("SYSTEM DATE;<yyyy-MM-dd>",
                "Sets the current system date used by every date-dependent operation.",
                p -> {
                    park.setDate(p[1]);
                    return "SYSTEM DATE SET TO " + park.today();
                });
        registry.register("SYSTEM PRICE;<Price>",
                "Sets the base price of tickets created from now on (this execution only; starts at 40.00).",
                p -> {
                    park.setPrice(p[1]);
                    return "SYSTEM PRICE SET TO " + park.basePrice() + " EUR";
                });
    }
}
