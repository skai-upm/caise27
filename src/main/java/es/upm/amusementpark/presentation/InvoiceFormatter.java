package es.upm.amusementpark.presentation;

import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceItem;
import es.upm.amusementpark.business.invoice.InvoiceSection;

/**
 * Renders invoices as text.
 */
final class InvoiceFormatter {

    private InvoiceFormatter() {
    }

    static String format(Invoice invoice) {
        List<String> lines = new ArrayList<>();
        lines.add(invoice.getTitle());
        lines.addAll(invoice.getHeader());
        for (InvoiceSection section : invoice.getSections()) {
            lines.add(section.title() + ":");
            for (InvoiceItem item : section.items()) {
                lines.add("  " + item.getDescription()
                        + item.getAmount().map(a -> ": " + a.toPlainString() + " EUR").orElse(""));
            }
        }
        lines.add("TOTAL: " + invoice.getTotal().toPlainString() + " EUR");
        return String.join(System.lineSeparator(), lines);
    }
}
