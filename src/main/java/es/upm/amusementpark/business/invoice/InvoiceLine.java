package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One invoiced item with its amount. Details describe the item (e.g. a group ticket's composition) and are not
 * charged separately.
 */
public record InvoiceLine(String description, BigDecimal amount, List<String> details) {

    public InvoiceLine(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }

    public InvoiceLine {
        details = List.copyOf(details);
    }
}
