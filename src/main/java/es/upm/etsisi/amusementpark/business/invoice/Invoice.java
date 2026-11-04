package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/** The product built by an {@link InvoiceBuilder}: a title, its applicable sections and the total. */
public class Invoice {

    private final String title;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, List<InvoiceSection> sections, BigDecimal total) {
        this.title = title;
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
