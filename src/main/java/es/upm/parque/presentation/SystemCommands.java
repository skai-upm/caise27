package es.upm.parque.presentation;

import es.upm.parque.business.controller.ParkController;

/**
 * SYSTEM DATE and SYSTEM PRICE.
 */
public class SystemCommands implements CommandSet {

    private final ParkController park;

    public SystemCommands(ParkController park) {
        this.park = park;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("SYSTEM DATE", "yyyy-MM-dd",
                "Sets the simulated current date used by every time-dependent rule.",
                p -> "OK SYSTEM DATE -> " + park.setSystemDate(p[0]));
        registry.register("SYSTEM PRICE", "Precio",
                "Sets the individual ticket price for new purchases (40.00 at start-up).",
                p -> "OK SYSTEM PRICE -> " + park.setTicketPrice(p[0]).toPlainString());
    }
}
