package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice. Only builders create invoices, step by step.
 */
public final class Invoice {

    private final String title;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, List<String> recipient, List<InvoiceSection> sections) {
        this.title = title;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceSection section : this.sections) {
            sum = sum.add(section.subtotal());
        }
        this.total = sum;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
