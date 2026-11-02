package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}. Invoices are generated on demand
 * and never persisted.
 */
public record Invoice(String title, List<String> header, List<InvoiceSection> sections) {

    public Invoice {
        header = List.copyOf(header);
        sections = List.copyOf(sections);
    }

    public BigDecimal total() {
        return sections.stream().map(InvoiceSection::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
