package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One charged item of an invoice, with optional descriptive detail lines that carry no amount.
 */
public record InvoiceLine(String description, BigDecimal amount, List<String> details) {

    public InvoiceLine {
        details = List.copyOf(details);
    }

    public InvoiceLine(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
