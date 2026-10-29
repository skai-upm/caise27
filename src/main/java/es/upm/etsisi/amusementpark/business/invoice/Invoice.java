package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}. The total is the sum of its line amounts.
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
}
