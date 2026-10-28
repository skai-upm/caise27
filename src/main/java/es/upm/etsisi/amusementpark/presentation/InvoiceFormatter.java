package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceItem;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceSection;

/**
 * Renders an invoice as readable text.
 */
public final class InvoiceFormatter {

    private InvoiceFormatter() {
    }

    public static String format(Invoice invoice) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("INVOICE " + invoice.getTitle());
        invoice.getRecipient().forEach(line -> text.append(nl).append(line));
        for (InvoiceSection section : invoice.getSections()) {
            text.append(nl).append('[').append(section.title()).append(']');
            for (InvoiceItem item : section.items()) {
                text.append(nl).append("  ").append(item.description()).append(" ... ").append(item.amount());
                item.details().forEach(detail -> text.append(nl).append("      - ").append(detail));
            }
        }
        return text.append(nl).append("TOTAL: ").append(invoice.getTotal()).toString();
    }
}
