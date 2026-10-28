package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An invoice, created only through an {@link InvoiceBuilder}. Not persisted.
 */
public final class Invoice {

    private final String title;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;

    Invoice(String title, List<String> recipient, List<InvoiceSection> sections) {
        this.title = title;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
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

    /** Total of the items shown in the invoice. */
    public BigDecimal getTotal() {
        return sections.stream().map(InvoiceSection::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }
}
