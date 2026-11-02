package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One charged item; details describe it without being charged (e.g. a group ticket's composition).
 */
public record InvoiceLine(String description, BigDecimal amount, List<String> details) {

    public InvoiceLine(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
