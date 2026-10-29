package es.upm.amusementpark.business.invoice;

import java.util.List;

/**
 * A titled group of invoice items. Invoices only contain non-empty sections.
 */
public record InvoiceSection(String title, List<InvoiceItem> items) {

    public InvoiceSection {
        items = List.copyOf(items);
    }
}
