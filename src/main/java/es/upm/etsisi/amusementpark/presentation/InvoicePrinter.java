package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceLine;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceSection;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders invoices as text.
 */
public final class InvoicePrinter {

    private InvoicePrinter() {
    }

    public static String print(Invoice invoice) {
        List<String> lines = new ArrayList<>();
        lines.add("===== " + invoice.getTitle() + " =====");
        lines.addAll(invoice.getHeader());
        for (InvoiceSection section : invoice.getSections()) {
            lines.add("-- " + section.title() + " --");
            for (InvoiceLine line : section.lines()) {
                lines.add("  " + line.description() + ": " + line.amount().toPlainString() + " EUR");
                for (String detail : line.details()) {
                    lines.add("      " + detail);
                }
            }
        }
        lines.add("TOTAL: " + invoice.getTotal().toPlainString() + " EUR");
        return String.join(System.lineSeparator(), lines);
    }
}
