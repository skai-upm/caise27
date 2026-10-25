package es.upm.parque.presentation.command;

import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceLine;
import es.upm.parque.business.invoice.InvoiceSection;
import es.upm.parque.presentation.CommandRegistry;

/**
 * INVOICE commands.
 */
public final class InvoiceCommands {

    private InvoiceCommands() {
    }

    public static void register(CommandRegistry registry, InvoiceController invoices) {
        registry.register("INVOICE CREATE;USER|GROUP;Correo|IdGrupo",
                "Shows an invoice: for a user, tickets bought directly and personal passes; "
                        + "for a group, its group tickets. Invoices are not stored.",
                p -> render(invoices.create(p[1], p[2])));
    }

    private static String render(Invoice invoice) {
        StringBuilder text = new StringBuilder("INVOICE ").append(invoice.recipient());
        for (InvoiceSection section : invoice.sections()) {
            text.append(System.lineSeparator()).append(section.title()).append(':');
            for (InvoiceLine line : section.lines()) {
                text.append(System.lineSeparator()).append("  ").append(line.description()).append(": ")
                        .append(line.amount().toPlainString());
            }
        }
        return text.append(System.lineSeparator()).append("TOTAL: ").append(invoice.total().toPlainString()).toString();
    }
}
