package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * A charged item of an invoice, with optional detail lines (e.g. a group ticket's composition).
 */
public record InvoiceItem(String description, BigDecimal amount, List<String> details) {

    public InvoiceItem {
        details = List.copyOf(details);
    }
}
