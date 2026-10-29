package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Common Builder abstraction for invoices. Concrete builders add their own construction steps and
 * use these shared ones; sections without lines are never added.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private final List<String> header = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected void addHeaderLine(String line) {
        header.add(line);
    }

    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }

    /**
     * Final step: creates the invoice with the total of every line shown.
     */
    public Invoice build() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                total = total.add(line.amount());
            }
        }
        return new Invoice(title, header, sections, total);
    }
}
