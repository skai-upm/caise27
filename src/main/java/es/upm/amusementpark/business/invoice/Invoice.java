package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Formats;

import java.math.BigDecimal;
import java.util.List;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}. Not persisted.
 */
public final class Invoice {

    private final String recipientType;
    private final String recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipientType, String recipient, List<InvoiceSection> sections) {
        this.recipientType = recipientType;
        this.recipient = recipient;
        this.sections = List.copyOf(sections);
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceSection section : this.sections) {
            sum = sum.add(section.subtotal());
        }
        this.total = sum;
    }

    public String getRecipientType() {
        return recipientType;
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

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVOICE - ").append(recipientType).append(": ").append(recipient);
        for (InvoiceSection section : sections) {
            sb.append('\n').append(section.title()).append(':');
            for (InvoiceLine line : section.lines()) {
                sb.append("\n  ").append(line.description()).append(" -> ").append(Formats.formatPrice(line.amount()));
            }
        }
        sb.append("\nTOTAL: ").append(Formats.formatPrice(total));
        return sb.toString();
    }
}
