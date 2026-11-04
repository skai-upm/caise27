package com.parque.business.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * A titled group of invoice lines (e.g. "Individual tickets") with its subtotal.
 */
public class InvoiceSection {
    private final String title;
    private final List<InvoiceLine> lines;

    public InvoiceSection(String title, List<InvoiceLine> lines) {
        this.title = title;
        this.lines = List.copyOf(lines);
    }

    public String getTitle() {
        return title;
    }

    /** Lines of the section (read-only). */
    public List<InvoiceLine> getLines() {
        return lines;
    }

    public BigDecimal getSubtotal() {
        BigDecimal sum = new BigDecimal("0.00");
        for (InvoiceLine line : lines) {
            sum = sum.add(line.getAmount());
        }
        return sum;
    }
}
