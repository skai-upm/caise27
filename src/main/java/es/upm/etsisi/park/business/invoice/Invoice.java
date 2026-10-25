package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}. Not persisted.
 */
public final class Invoice {

    private static final String NEWLINE = System.lineSeparator();

    private final String title;
    private final LocalDate issueDate;
    private final Map<String, String> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, LocalDate issueDate, Map<String, String> recipient, List<InvoiceSection> sections,
            BigDecimal total) {
        this.title = title;
        this.issueDate = issueDate;
        this.recipient = Collections.unmodifiableMap(new LinkedHashMap<>(recipient));
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public Map<String, String> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String toText() {
        StringBuilder text = new StringBuilder();
        text.append("===== ").append(title).append(" =====");
        if (issueDate != null) {
            text.append(NEWLINE).append("Date: ").append(issueDate);
        }
        for (Map.Entry<String, String> entry : recipient.entrySet()) {
            text.append(NEWLINE).append(entry.getKey()).append(": ").append(entry.getValue());
        }
        for (InvoiceSection section : sections) {
            text.append(NEWLINE).append("-- ").append(section.title()).append(" --");
            for (InvoiceLine line : section.lines()) {
                text.append(NEWLINE);
                if (line.isPriced()) {
                    text.append("  ").append(line.description()).append(" ... ")
                            .append(line.amount().toPlainString()).append(" EUR");
                } else {
                    text.append("      ").append(line.description());
                }
            }
        }
        text.append(NEWLINE).append("TOTAL: ").append(total.toPlainString()).append(" EUR");
        return text.toString();
    }
}
