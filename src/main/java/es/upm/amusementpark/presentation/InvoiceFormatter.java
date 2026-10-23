package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceItem;
import es.upm.amusementpark.business.invoice.InvoiceSection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders an {@link Invoice} as readable text.
 */
public class InvoiceFormatter {

    public String format(Invoice invoice) {
        List<String> lines = new ArrayList<>();
        lines.add("=== " + invoice.getTitle().toUpperCase(Locale.ROOT) + " ===");
        lines.add("Date: " + invoice.getDate());
        lines.addAll(invoice.getHeader());
        for (InvoiceSection section : invoice.getSections()) {
            lines.add(section.title() + ":");
            for (InvoiceItem item : section.items()) {
                lines.add("  " + item.description() + " ... " + Park.formatAmount(item.amount()) + " EUR");
                for (String detail : item.details()) {
                    lines.add("      " + detail);
                }
            }
        }
        lines.add("TOTAL: " + Park.formatAmount(invoice.getTotal()) + " EUR");
        return String.join(System.lineSeparator(), lines);
    }
}
