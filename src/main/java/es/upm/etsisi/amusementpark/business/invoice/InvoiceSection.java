package es.upm.etsisi.amusementpark.business.invoice;

import java.util.List;

/**
 * A titled group of invoice lines. Sections are only created when they have lines.
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }
}
