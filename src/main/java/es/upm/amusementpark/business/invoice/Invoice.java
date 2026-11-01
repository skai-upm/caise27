package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}. Invoices are
 * generated on demand and never persisted.
 */
public final class Invoice {

    private final String recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipient, List<InvoiceSection> sections, BigDecimal total) {
        this.recipient = recipient;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
