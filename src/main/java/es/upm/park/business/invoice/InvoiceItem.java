package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A charged line of an invoice. Details describe the item (for example the
 * tickets composing a group ticket) and are never charged themselves.
 */
public record InvoiceItem(String description, BigDecimal amount, List<String> details) {

    public InvoiceItem {
        details = List.copyOf(details);
    }

    public InvoiceItem(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
