package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of invoice lines.
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }

    public BigDecimal subtotal() {
        return lines.stream().map(InvoiceLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
