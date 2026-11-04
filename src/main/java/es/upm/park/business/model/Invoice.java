package es.upm.park.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An on-demand invoice: a recipient, the issue date, the sections that apply to the recipient
 * and the total of all included lines. Built by an invoice builder.
 */
public final class Invoice {

    /** One charged item. */
    public record Line(String description, BigDecimal amount) {
    }

    /** A titled group of lines. */
    public record Section(String title, List<Line> lines) {
        public Section {
            lines = List.copyOf(lines);
        }
    }

    private final String recipient;
    private final LocalDate date;
    private final List<Section> sections;

    public Invoice(String recipient, LocalDate date, List<Section> sections) {
        this.recipient = recipient;
        this.date = date;
        this.sections = List.copyOf(sections);
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

    /** Sum of every line of every section, with two decimals. */
    public BigDecimal getTotal() {
        return sections.stream().flatMap(s -> s.lines().stream())
                .map(Line::amount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
    }

    /** Printable multi-line text of the invoice. */
    public String format() {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder("INVOICE ").append(recipient).append(nl)
                .append("Date: ").append(date).append(nl);
        for (Section section : sections) {
            text.append(section.title()).append(':').append(nl);
            for (Line line : section.lines()) {
                text.append("  ").append(line.description()).append(' ')
                        .append(line.amount().toPlainString()).append(nl);
            }
        }
        return text.append("Total: ").append(getTotal().toPlainString()).toString();
    }
}
