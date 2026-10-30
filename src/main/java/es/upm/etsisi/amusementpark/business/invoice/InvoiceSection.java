package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of items. Only charged sections add to the invoice total; the others describe
 * what a charged item includes.
 */
public record InvoiceSection(String title, List<InvoiceItem> items, boolean charged) {

    public InvoiceSection {
        items = List.copyOf(items);
    }

    public BigDecimal subtotal() {
        return items.stream().map(InvoiceItem::amount).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
