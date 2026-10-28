package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of invoice items.
 */
public record InvoiceSection(String title, List<InvoiceItem> items) {

    public InvoiceSection {
        items = List.copyOf(items);
    }

    public BigDecimal subtotal() {
        return items.stream().map(InvoiceItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
