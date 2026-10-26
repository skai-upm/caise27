package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of invoice lines of the same kind.
 */
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
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            subtotal = subtotal.add(line.getAmount());
        }
        return subtotal;
    }
}
