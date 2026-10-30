package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}. Not persisted.
 */
public final class Invoice {

    private final String title;
    private final LocalDate issueDate;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, LocalDate issueDate, List<String> recipient, List<InvoiceSection> sections,
            BigDecimal total) {
        this.title = title;
        this.issueDate = issueDate;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getIssueDate() {
        return issueDate;
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
