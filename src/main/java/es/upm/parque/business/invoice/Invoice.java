package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An invoice generated on demand. Only built through an {@link InvoiceBuilder}; never persisted.
 */
public record Invoice(String recipient, List<InvoiceSection> sections, BigDecimal total) {

    public Invoice {
        sections = List.copyOf(sections);
    }
}
