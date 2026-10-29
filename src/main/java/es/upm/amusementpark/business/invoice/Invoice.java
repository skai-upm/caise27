package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/** An invoice produced by an {@link InvoiceBuilder}; it is never persisted. */
public final class Invoice {

    private final String title;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, List<InvoiceSection> sections, BigDecimal total) {
        this.title = title;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** Legible text rendering: title, each section with its lines, and the total. */
    public String toText() {
        StringBuilder text = new StringBuilder("=== ").append(title).append(" ===\n");
        for (InvoiceSection section : sections) {
            text.append(section.getTitle()).append(":\n");
            for (InvoiceLine line : section.getLines()) {
                text.append(line.isCharged() ? "  " : "      ").append(line.getDescription());
                if (line.isCharged()) {
                    text.append(" ... ").append(line.getAmount()).append(" EUR");
                }
                text.append('\n');
            }
        }
        return text.append("TOTAL: ").append(total).append(" EUR").toString();
    }
}
