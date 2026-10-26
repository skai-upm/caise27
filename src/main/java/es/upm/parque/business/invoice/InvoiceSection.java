package es.upm.parque.business.invoice;

import java.util.List;

/**
 * A titled group of invoice items, such as "Individual tickets".
 *
 * @param title section heading
 * @param lines items of the section
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }
}
