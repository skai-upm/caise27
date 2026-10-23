package es.upm.etsisi.park.presentation.command;

import es.upm.etsisi.park.business.controller.InvoiceController;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** INVOICE CREATE for users and groups. */
public class InvoiceCommands implements CommandGroup {

    static final String USER = "INVOICE CREATE;USER;<email>";
    static final String GROUP = "INVOICE CREATE;GROUP;<groupIdentifier>";

    private final InvoiceController invoices;

    public InvoiceCommands(InvoiceController invoices) {
        this.invoices = invoices;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("INVOICE CREATE",
                "Prints an invoice: a user's direct tickets and passes, or a group's group tickets, with the total.",
                (args, out) -> {
                    Args.require(args, USER + " | " + GROUP, 2);
                    out.println(invoices.create(args[0], args[1]).toText());
                }, USER, GROUP));
    }
}
