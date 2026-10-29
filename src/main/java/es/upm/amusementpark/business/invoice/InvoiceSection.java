package es.upm.amusementpark.business.invoice;

import java.util.List;

/**
 * A titled group of charged items in an invoice.
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }
}
