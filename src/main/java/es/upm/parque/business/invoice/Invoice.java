package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An invoice generated on demand. Instances are assembled by an {@link InvoiceBuilder}.
 */
public final class Invoice {

    private final String title;
    private final String recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, String recipient, List<InvoiceSection> sections, BigDecimal total) {
        this.title = title;
        this.recipient = recipient;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
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
     * Renders the invoice as text lines (without the command status line).
     */
    public List<String> toLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Invoice: " + title);
        lines.add("Recipient: " + recipient);
        if (sections.isEmpty()) {
            lines.add("No billable items");
        }
        for (InvoiceSection section : sections) {
            lines.add(section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                lines.add("  - " + line.description() + ": " + line.amount().toPlainString());
            }
        }
        lines.add("TOTAL: " + total.toPlainString());
        return lines;
    }
}
