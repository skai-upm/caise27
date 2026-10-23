package es.upm.etsisi.park.presentation.command;

import es.upm.etsisi.park.business.controller.SystemController;
import es.upm.etsisi.park.business.model.ticket.Money;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** SYSTEM DATE and SYSTEM PRICE. */
public class SystemCommands implements CommandGroup {

    static final String DATE = "SYSTEM DATE;<yyyy-MM-dd>";
    static final String PRICE = "SYSTEM PRICE;<price>";

    private final SystemController system;

    public SystemCommands(SystemController system) {
        this.system = system;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("SYSTEM DATE",
                "Sets the current park date used by every date-dependent operation (initially today).",
                (args, out) -> {
                    Args.require(args, DATE, 1);
                    out.println("SYSTEM DATE SET TO " + system.setDate(args[0]));
                }, DATE));
        registry.register(CommandDefinition.of("SYSTEM PRICE",
                "Sets the base price of new tickets for this execution (initially 40.00 EUR).",
                (args, out) -> {
                    Args.require(args, PRICE, 1);
                    out.println("SYSTEM PRICE SET TO " + Money.format(system.setBasePrice(args[0])));
                }, PRICE));
    }
}
