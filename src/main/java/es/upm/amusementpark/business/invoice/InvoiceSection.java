package es.upm.amusementpark.business.invoice;

import java.util.List;

/** A titled group of invoice lines. */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }
}
