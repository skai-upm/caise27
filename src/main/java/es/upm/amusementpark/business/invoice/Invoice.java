package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}.
 */
public final class Invoice {

    /** Who the invoice is addressed to. */
    public enum Type {
        USER,
        GROUP
    }

    private final Type type;
    private final List<Map.Entry<String, String>> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(Type type, List<Map.Entry<String, String>> recipient, List<InvoiceSection> sections, BigDecimal total) {
        this.type = type;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public Type getType() {
        return type;
    }

    /** Identification lines of the recipient as (label, value) pairs, in order. */
    public List<Map.Entry<String, String>> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
