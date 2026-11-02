package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of invoice lines.
 */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }

    public BigDecimal total() {
        return lines.stream().filter(InvoiceLine::charged).map(InvoiceLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
