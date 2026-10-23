package es.upm.park.business.invoice;

import java.util.List;

/**
 * A titled group of invoice items, e.g. "Individual tickets".
 */
public record InvoiceSection(String title, List<InvoiceItem> items) {

    public InvoiceSection {
        items = List.copyOf(items);
    }
}
