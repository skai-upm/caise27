package es.upm.parque.business.invoice;

import java.util.List;

/**
 * A titled group of invoice lines, e.g. "Individual tickets".
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }
}
