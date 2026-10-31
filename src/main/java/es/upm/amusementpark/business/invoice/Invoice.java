package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice. Instances are produced by an {@link InvoiceBuilder};
 * only the sections that apply to the recipient are present.
 */
public final class Invoice {

    private final String recipientType;
    private final String recipientId;
    private final String recipientName;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipientType, String recipientId, String recipientName,
            List<InvoiceSection> sections, BigDecimal total) {
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
