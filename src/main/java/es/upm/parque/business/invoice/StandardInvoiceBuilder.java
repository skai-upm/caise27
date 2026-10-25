package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link InvoiceBuilder}: keeps the added non-empty sections and sums their lines.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String recipient;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder start(String recipient) {
        this.recipient = recipient;
        sections.clear();
        return this;
    }

    @Override
    public InvoiceBuilder addSection(String title, List<InvoiceLine> lines) {
        requireStarted();
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
        return this;
    }

    @Override
    public Invoice build() {
        requireStarted();
        BigDecimal total = sections.stream()
                .flatMap(s -> s.lines().stream())
                .map(InvoiceLine::amount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        return new Invoice(recipient, sections, total);
    }

    private void requireStarted() {
        if (recipient == null) {
            throw new IllegalStateException("Invoice recipient not set: call start first");
        }
    }
}
