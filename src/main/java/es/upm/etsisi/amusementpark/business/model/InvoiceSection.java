package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.util.List;

/** A titled group of invoice lines, e.g. "Individual tickets". */
public class InvoiceSection {

    private final String title;
    private final List<InvoiceLine> lines;

    public InvoiceSection(String title, List<InvoiceLine> lines) {
        this.title = title;
        this.lines = List.copyOf(lines);
    }

    public String getTitle() {
        return title;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }

    public BigDecimal getSubtotal() {
        return lines.stream().map(InvoiceLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
