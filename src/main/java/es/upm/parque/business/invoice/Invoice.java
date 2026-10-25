package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An invoice generated on demand. Instances are produced by an {@link InvoiceBuilder}.
 */
public record Invoice(String recipient, List<InvoiceSection> sections, BigDecimal total) {

    public Invoice {
        sections = List.copyOf(sections);
    }
}
