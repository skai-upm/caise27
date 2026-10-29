package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A charged line of an invoice, optionally with descriptive detail lines that carry no amount.
 */
public record InvoiceItem(String description, BigDecimal amount, List<String> details) {

    public InvoiceItem {
        details = List.copyOf(details);
    }

    public InvoiceItem(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
