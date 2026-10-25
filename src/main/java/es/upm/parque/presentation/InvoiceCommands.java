package es.upm.parque.presentation;

import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceLine;
import es.upm.parque.business.invoice.InvoiceSection;

/**
 * INVOICE CREATE for users and groups.
 */
public class InvoiceCommands implements CommandSet {

    private final InvoiceController invoices;

    public InvoiceCommands(InvoiceController invoices) {
        this.invoices = invoices;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("INVOICE CREATE;USER", "Correo",
                "Shows the user's invoice: tickets bought directly and personal passes.",
                p -> render(invoices.userInvoice(p[0])));
        registry.register("INVOICE CREATE;GROUP", "IdGrupo",
                "Shows the group's invoice: its group-ticket purchases.",
                p -> render(invoices.groupInvoice(p[0])));
    }

    private static String render(Invoice invoice) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("INVOICE ").append(invoice.recipient());
        for (InvoiceSection section : invoice.sections()) {
            text.append(nl).append("  ").append(section.title()).append(':');
            for (InvoiceLine line : section.lines()) {
                text.append(nl).append("    ").append(line.description()).append(" -> ")
                        .append(line.amount().toPlainString());
            }
        }
        return text.append(nl).append("  TOTAL: ").append(invoice.total().toPlainString()).toString();
    }
}
