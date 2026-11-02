package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.invoice.InvoiceController;
import java.util.List;
import java.util.Set;

/**
 * INVOICE CREATE;USER|GROUP;...
 */
public final class InvoiceCommands {

    private InvoiceCommands() {
    }

    public static void register(CommandRegistry registry, InvoiceController invoices, InvoiceFormatter formatter) {
        registry.register(new CommandSpec("INVOICE CREATE",
                List.of("INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>"),
                "Shows the invoice of a user (direct tickets and personal passes) or of a group (group tickets).",
                Set.of(3), p -> switch (p[1]) {
                    case "USER" -> CommandResult.message(formatter.format(invoices.userInvoice(p[2])));
                    case "GROUP" -> CommandResult.message(formatter.format(invoices.groupInvoice(p[2])));
                    default -> throw new CommandSyntaxException("invoice recipient must be USER or GROUP");
                }));
    }
}
