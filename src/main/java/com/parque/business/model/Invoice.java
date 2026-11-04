package com.parque.business.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * An invoice generated on demand for a user or a group: the recipient, the applicable sections and the total.
 * Built step by step by an invoice builder; never persisted.
 */
public class Invoice {
    private final String recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    public Invoice(String recipient, List<InvoiceSection> sections) {
        this.recipient = recipient;
        this.sections = List.copyOf(sections);
        BigDecimal sum = new BigDecimal("0.00");
        for (InvoiceSection s : this.sections) {
            sum = sum.add(s.getSubtotal());
        }
        this.total = sum;
    }

    public String getRecipient() {
        return recipient;
    }

    /** Applicable, non-empty sections in construction order (read-only). */
    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
