package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of invoice lines, e.g. the individual tickets of a user.
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
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (InvoiceLine line : lines) {
            sum = sum.add(line.getAmount());
        }
        return sum;
    }
}
