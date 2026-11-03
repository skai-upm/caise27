package es.upm.etsisi.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** An on-demand invoice: recipient, issue date and the applicable sections. It is never persisted. */
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

    /** Sum of every line of every section, with two decimals. */
    public BigDecimal getTotal() {
        return sections.stream().map(InvoiceSection::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
