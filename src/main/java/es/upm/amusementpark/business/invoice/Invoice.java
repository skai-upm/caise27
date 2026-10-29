package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}. It is generated on demand and never
 * persisted.
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

    /** Human-readable text rendering. */
    public String render() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("===== ").append(title).append(" =====").append(nl);
        for (String line : header) {
            sb.append(line).append(nl);
        }
        for (InvoiceSection section : sections) {
            sb.append("-- ").append(section.title()).append(" --").append(nl);
            for (InvoiceItem item : section.items()) {
                sb.append("  ").append(item.description()).append("  ")
                        .append(item.amount().toPlainString()).append(" EUR").append(nl);
                for (String detail : item.details()) {
                    sb.append("      ").append(detail).append(nl);
                }
            }
        }
        sb.append("TOTAL: ").append(total.toPlainString()).append(" EUR");
        return sb.toString();
    }
}
