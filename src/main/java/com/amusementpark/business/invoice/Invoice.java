package com.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * An invoice generated on demand; it is never stored.
 */
public class Invoice {
    private final String recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String recipient, List<InvoiceSection> sections) {
        this.recipient = recipient;
        this.sections = List.copyOf(sections);
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                sum = sum.add(line.amount());
            }
        }
        this.total = sum.setScale(2, RoundingMode.HALF_UP);
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

    public String render() {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("INVOICE ").append(recipient);
        if (sections.isEmpty()) {
            text.append(nl).append("  No purchases");
        }
        for (InvoiceSection section : sections) {
            text.append(nl).append(section.title());
            for (InvoiceLine line : section.lines()) {
                text.append(nl).append("  ").append(line.description()).append(": ")
                        .append(line.amount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            }
        }
        return text.append(nl).append("TOTAL: ").append(total.toPlainString()).toString();
    }
}
