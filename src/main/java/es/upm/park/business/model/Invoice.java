package es.upm.park.business.model;

import java.math.BigDecimal;
import java.util.List;

/** An invoice generated on demand for a user or a group; it is never persisted. */
public record Invoice(String recipient, List<InvoiceSection> sections) {

    public Invoice {
        sections = List.copyOf(sections);
    }

    public BigDecimal total() {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            sum = sum.add(section.subtotal());
        }
        return sum;
    }
}
