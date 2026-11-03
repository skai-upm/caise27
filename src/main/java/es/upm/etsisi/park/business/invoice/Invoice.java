package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An invoice generated on demand. Immutable; built only through an {@link InvoiceBuilder}.
 */
public final class Invoice {

    /** A titled group of invoice lines. */
    public record Section(String title, List<Line> lines) {
    }

    /** An invoice line; {@code amount} is {@code null} for informative details. */
    public record Line(String text, BigDecimal amount) {
    }

    private final String title;
    private final List<String> header;
    private final List<Section> sections;
    private final BigDecimal total;

    Invoice(String title, List<String> header, List<Section> sections, BigDecimal total) {
        this.title = title;
        this.header = List.copyOf(header);
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<Section> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String toText() {
        List<String> out = new ArrayList<>();
        out.add("=== " + title + " ===");
        out.addAll(header);
        for (Section section : sections) {
            out.add("-- " + section.title() + " --");
            for (Line line : section.lines()) {
                out.add(line.amount() == null ? "    " + line.text() : line.text() + ": " + line.amount() + " EUR");
            }
        }
        out.add("TOTAL: " + total + " EUR");
        return String.join(System.lineSeparator(), out);
    }
}
