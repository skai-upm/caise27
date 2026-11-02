package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceLine;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceSection;
import es.upm.etsisi.amusementpark.business.park.Prices;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an invoice as text.
 */
public class InvoiceFormatter {

    public String format(Invoice invoice) {
        List<String> lines = new ArrayList<>();
        lines.add(invoice.title());
        lines.addAll(invoice.header());
        for (InvoiceSection section : invoice.sections()) {
            lines.add("[" + section.title() + "]");
            for (InvoiceLine line : section.lines()) {
                lines.add(line.charged()
                        ? "  " + line.description() + ": " + Prices.format(line.amount())
                        : "    - " + line.description() + " (" + Prices.format(line.amount()) + ", included)");
            }
        }
        lines.add("TOTAL: " + Prices.format(invoice.total()));
        return String.join(System.lineSeparator(), lines);
    }
}
