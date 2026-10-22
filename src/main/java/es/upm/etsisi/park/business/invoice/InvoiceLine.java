package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One charged item. {@code details} are informative rows (e.g. group-ticket composition)
 * that do not add to the total.
 */
public record InvoiceLine(String description, BigDecimal amount, List<String> details) {

    public InvoiceLine {
        details = List.copyOf(details);
    }

    public InvoiceLine(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
