package es.upm.etsisi.park.business.model.invoice;

import java.math.BigDecimal;
import java.util.List;

/** A titled group of invoice lines. */
public record InvoiceSection(String title, List<InvoiceLine> lines) {

    public InvoiceSection {
        lines = List.copyOf(lines);
    }

    public BigDecimal subtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            if (line.isItem()) {
                total = total.add(line.amount());
            }
        }
        return total;
    }
}
