package com.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An invoice produced by an {@link InvoiceBuilder}: a header, the applicable sections and a total.
 */
public final class Invoice {

    /** One invoiced item. */
    public record Line(String description, BigDecimal amount) {
    }

    /** A titled group of items, present only when it has items. */
    public record Section(String title, List<Line> lines) {
    }

    private final String recipient;
    private final LocalDate date;
    private final List<Section> sections;
    private final BigDecimal total;

    Invoice(String recipient, LocalDate date, List<Section> sections, BigDecimal total) {
        this.recipient = recipient;
        this.date = date;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getRecipient() {
        return recipient;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Section> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String toText() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("INVOICE - ").append(recipient).append(nl)
                .append("Date: ").append(date).append(nl);
        for (Section section : sections) {
            sb.append(section.title()).append(':').append(nl);
            for (Line line : section.lines()) {
                sb.append("  ").append(line.description()).append("  ").append(line.amount().toPlainString()).append(nl);
            }
        }
        return sb.append("TOTAL: ").append(total.toPlainString()).toString();
    }
}
