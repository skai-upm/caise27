package es.upm.amusementpark.business.invoice;

import java.util.List;

/**
 * A titled group of invoice items.
 */
public record InvoiceSection(String title, List<InvoiceItem> items) {

    public InvoiceSection {
        items = List.copyOf(items);
    }
}
