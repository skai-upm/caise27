package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Common abstraction for building invoices step by step. Concrete builders add the
 * steps that make sense for their recipient; sections without lines are omitted and
 * the total is the sum of the lines shown.
 */
public abstract class InvoiceBuilder {
    private final String title;
    private final List<String> recipient = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected void addRecipientLine(String line) {
        recipient.add(line);
    }

    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, List.copyOf(lines)));
        }
    }

    public Invoice build() {
        BigDecimal total = sections.stream()
                .flatMap(s -> s.lines().stream())
                .map(InvoiceLine::amount)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new Invoice(title, recipient, sections, total);
    }
}
