package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One charged item. {@code details} describe it (e.g. a group ticket's composition) and are not
 * charged separately.
 */
public record InvoiceLine(String description, BigDecimal amount, List<String> details) {

    public InvoiceLine {
        details = List.copyOf(details);
    }

    public InvoiceLine(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
