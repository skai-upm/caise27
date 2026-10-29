package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Common abstraction for building invoices step by step. Concrete builders
 * offer only the steps meaningful for their recipient type; sections without
 * lines are omitted from the result.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    /** Adds a section; it is skipped when it has no lines. */
    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }

    /** Builds the invoice; the total is the sum of all charged lines shown. */
    public Invoice build() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.getLines()) {
                if (line.isCharged()) {
                    total = total.add(line.getAmount());
                }
            }
        }
        return new Invoice(title, sections, total);
    }
}
