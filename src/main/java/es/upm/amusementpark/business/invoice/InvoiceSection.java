package es.upm.amusementpark.business.invoice;

import java.util.List;
import java.util.Objects;

/** A titled group of invoice lines. */
public final class InvoiceSection {

    private final String title;
    private final List<InvoiceLine> lines;

    InvoiceSection(String title, List<InvoiceLine> lines) {
        this.title = Objects.requireNonNull(title);
        this.lines = List.copyOf(lines);
    }

    public String getTitle() {
        return title;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }
}
