package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An invoice assembled by an {@link InvoiceBuilder}: the recipient, the applicable sections and the
 * total of all their items. Invoices are generated on demand and never stored.
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
