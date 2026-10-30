package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import es.upm.amusementpark.business.model.ParkFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Text presentation of an invoice.
 */
public class InvoiceRenderer {

    public String render(Invoice invoice) {
        List<String> lines = new ArrayList<>();
        lines.add("===== " + invoice.getTitle() + " =====");
        lines.add("Date: " + ParkFormat.formatDate(invoice.getIssueDate()));
        lines.addAll(invoice.getRecipient());
        for (InvoiceSection section : invoice.getSections()) {
            lines.add("-- " + section.title() + " --");
            for (InvoiceLine line : section.lines()) {
                lines.add("  " + line.description() + ": " + ParkFormat.formatPrice(line.amount()) + " EUR");
                for (String detail : line.details()) {
                    lines.add("      " + detail);
                }
            }
        }
        lines.add("TOTAL: " + ParkFormat.formatPrice(invoice.getTotal()) + " EUR");
        return String.join(System.lineSeparator(), lines);
    }
}
