package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}. Not persisted.
 */
public final class Invoice {
    private final String title;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, List<String> recipient, List<InvoiceSection> sections, BigDecimal total) {
        this.title = title;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
        this.total = total;
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

    public String render() {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder(title);
        recipient.forEach(r -> text.append(nl).append("  ").append(r));
        for (InvoiceSection section : sections) {
            text.append(nl).append(section.title());
            for (InvoiceLine line : section.lines()) {
                text.append(nl).append("  ").append(line.description()).append(": ")
                        .append(line.amount().toPlainString()).append(" EUR");
                line.details().forEach(d -> text.append(nl).append("    - ").append(d));
            }
        }
        return text.append(nl).append("TOTAL: ").append(total.toPlainString()).append(" EUR").toString();
    }
}
