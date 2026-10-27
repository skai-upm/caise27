package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An invoice built by an {@link InvoiceBuilder}. It is generated on demand
 * and not persisted.
 */
public final class Invoice {

    private final String title;
    private final List<String> header;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, List<String> header, List<InvoiceSection> sections, BigDecimal total) {
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

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** The invoice as text lines. */
    public List<String> render() {
        List<String> lines = new ArrayList<>();
        lines.add(title);
        lines.addAll(header);
        for (InvoiceSection section : sections) {
            lines.add(section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                lines.add("  " + line.description() + ": " + line.amount() + " EUR");
                for (String detail : line.details()) {
                    lines.add("    " + detail);
                }
            }
        }
        lines.add("TOTAL: " + total + " EUR");
        return lines;
    }
}
