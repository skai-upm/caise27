package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;

/**
 * INVOICE CREATE for users and groups.
 */
public class InvoiceCommands implements CommandModule {

    private final InvoiceController invoices;

    public InvoiceCommands(InvoiceController invoices) {
        this.invoices = invoices;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("INVOICE CREATE;USER;<email>",
                "Prints the invoice of an active user: direct tickets and personal passes.",
                p -> invoices.create(p[1], p[2]).toText());
        registry.register("INVOICE CREATE;GROUP;<groupIdentifier>",
                "Prints the invoice of a group: its group tickets and their composition.",
                p -> invoices.create(p[1], p[2]).toText());
    }
}
