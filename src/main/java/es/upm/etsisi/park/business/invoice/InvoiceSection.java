package es.upm.etsisi.park.business.invoice;

import java.util.List;

/**
 * Titled group of invoice lines. Only sections with lines are added to an invoice.
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }
}
