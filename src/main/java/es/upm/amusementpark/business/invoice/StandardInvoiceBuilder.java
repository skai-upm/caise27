package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link InvoiceBuilder}: keeps only non-empty sections and computes
 * the total as the sum of their lines.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String recipient;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder forRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public InvoiceBuilder addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
        return this;
    }

    @Override
    public Invoice build() {
        if (recipient == null) {
            throw new IllegalStateException("the invoice recipient must be set before building");
        }
        BigDecimal total = sections.stream().map(InvoiceSection::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        return new Invoice(recipient, sections, total);
    }
}
