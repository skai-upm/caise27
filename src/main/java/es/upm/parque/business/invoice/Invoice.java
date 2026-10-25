package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An invoice produced by an {@link InvoiceBuilder}: a header, the applicable sections and the total.
 */
public class Invoice {

    private final String header;
    private final List<InvoiceSection> sections;

    public Invoice(String header, List<InvoiceSection> sections) {
        this.header = header;
        this.sections = List.copyOf(sections);
    }

    public String getHeader() {
        return header;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            total = total.add(section.subtotal());
        }
        return total;
    }

    /**
     * Text lines of the invoice, ready to display.
     */
    public List<String> render() {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        for (InvoiceSection section : sections) {
            lines.add("  " + section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                lines.add("    " + line.description() + " " + line.amount().toPlainString());
            }
        }
        lines.add("  TOTAL: " + getTotal().toPlainString());
        return lines;
    }
}
