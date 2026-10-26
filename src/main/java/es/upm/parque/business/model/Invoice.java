package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An invoice generated on demand. It is assembled by an invoice builder and is immutable once built.
 */
public class Invoice {

    private final String recipient;
    private final LocalDate issueDate;
    private final List<InvoiceSection> sections;

    public Invoice(String recipient, LocalDate issueDate, List<InvoiceSection> sections) {
        this.recipient = recipient;
        this.issueDate = issueDate;
        this.sections = List.copyOf(sections);
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
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceSection section : sections) {
            total = total.add(section.getSubtotal());
        }
        return total;
    }
}
