package es.upm.parque.presentation.command;

import es.upm.parque.business.controller.ParkController;
import es.upm.parque.presentation.CommandRegistry;

/**
 * SYSTEM commands: simulated date and ticket price.
 */
public final class SystemCommands {

    private SystemCommands() {
    }

    public static void register(CommandRegistry registry, ParkController park) {
        registry.register("SYSTEM DATE;yyyy-MM-dd",
                "Sets the current date used by every date rule (initially today's date).",
                p -> "OK SYSTEM DATE -> " + park.setSystemDate(p[1]));
        registry.register("SYSTEM PRICE;Precio",
                "Sets the price of individual tickets bought from now on (initially 40.00).",
                p -> "OK SYSTEM PRICE -> " + park.setTicketPrice(p[1]).toPlainString());
    }
}
