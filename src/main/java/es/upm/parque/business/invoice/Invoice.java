package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An invoice generated on demand. Instances are created by an {@link InvoiceBuilder}.
 */
public class Invoice {
    private static final int LINE_WIDTH = 40;

    private final String recipientType;
    private final String recipientId;
    private final String recipientName;
    private final LocalDate issueDate;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipientType, String recipientId, String recipientName, LocalDate issueDate,
            List<InvoiceSection> sections, BigDecimal total) {
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.issueDate = issueDate;
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

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String format() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("INVOICE ").append(recipientType).append(' ').append(recipientId).append(" - ").append(recipientName)
                .append(nl).append("Date: ").append(issueDate);
        for (InvoiceSection section : sections) {
            sb.append(nl).append(section.getTitle()).append(':');
            for (InvoiceLine line : section.getLines()) {
                String amount = line.getAmount().toPlainString();
                String text = "  " + line.getDescription() + " ";
                sb.append(nl).append(text).append(".".repeat(Math.max(2, LINE_WIDTH - text.length() - amount.length())))
                        .append(' ').append(amount);
            }
        }
        return sb.append(nl).append("TOTAL: ").append(total.toPlainString()).toString();
    }
}
