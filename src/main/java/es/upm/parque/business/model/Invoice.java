package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * An invoice produced by an {@link InvoiceBuilder}. It is immutable and is not persisted.
 */
public class Invoice {

    private final String recipientKind;
    private final String recipientId;
    private final String recipientName;
    private final LocalDate issueDate;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipientKind, String recipientId, String recipientName, LocalDate issueDate,
            List<InvoiceSection> sections, BigDecimal total) {
        this.recipientKind = recipientKind;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.issueDate = issueDate;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getRecipientKind() {
        return recipientKind;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /**
     * Text rendering: header, date, one block per section and the total.
     */
    public String toText() {
        List<String> lines = new ArrayList<>();
        lines.add("INVOICE " + recipientKind + " " + recipientId + " - " + recipientName);
        lines.add("Date: " + SystemDate.format(issueDate));
        if (sections.isEmpty()) {
            lines.add("No purchases");
        }
        for (InvoiceSection section : sections) {
            lines.add(section.getTitle() + ":");
            for (InvoiceLine line : section.getLines()) {
                lines.add("- " + line.getDescription() + " " + line.getAmount().toPlainString());
            }
        }
        lines.add("TOTAL: " + total.toPlainString());
        return String.join(System.lineSeparator(), lines);
    }
}
