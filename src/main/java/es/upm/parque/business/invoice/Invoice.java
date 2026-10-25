package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}. Invoices are generated on demand and not stored.
 */
public class Invoice {

    private final String recipient;
    private final LocalDate issueDate;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipient, LocalDate issueDate, List<InvoiceSection> sections, BigDecimal total) {
        this.recipient = recipient;
        this.issueDate = issueDate;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getRecipient() {
        return recipient;
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

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("INVOICE").append(nl)
                .append("  Recipient: ").append(recipient).append(nl)
                .append("  Date: ").append(issueDate);
        if (sections.isEmpty()) {
            sb.append(nl).append("  (no items)");
        }
        for (InvoiceSection section : sections) {
            sb.append(nl).append("  ").append(section.title()).append(':');
            for (InvoiceLine line : section.lines()) {
                sb.append(nl).append("    - ").append(line.description()).append(": ").append(format(line.price()));
            }
        }
        return sb.append(nl).append("  TOTAL: ").append(format(total)).toString();
    }

    private static String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
