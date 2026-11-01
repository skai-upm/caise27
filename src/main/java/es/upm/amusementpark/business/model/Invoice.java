package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An immutable invoice. Instances are created by an {@link InvoiceBuilder}.
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

    /**
     * Plain-text representation shown to the user.
     */
    public String render() {
        List<String> lines = new ArrayList<>();
        lines.add("INVOICE " + recipient);
        for (InvoiceSection section : sections) {
            lines.add(section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                lines.add("  " + line.description() + ": " + line.amount().toPlainString());
            }
        }
        lines.add("TOTAL: " + total.toPlainString());
        return String.join(System.lineSeparator(), lines);
    }
}
