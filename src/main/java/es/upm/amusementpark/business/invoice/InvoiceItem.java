package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One charged line of an invoice, optionally followed by descriptive detail lines that are not charged.
 */
public record InvoiceItem(String description, BigDecimal amount, List<String> details) {

    public InvoiceItem {
        details = List.copyOf(details);
    }
}
