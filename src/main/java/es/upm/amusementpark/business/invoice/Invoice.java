package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An immutable invoice, obtained from an {@link InvoiceBuilder}.
 */
public final class Invoice {

    private final String title;
    private final LocalDate date;
    private final List<String> header;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, LocalDate date, List<String> header, List<InvoiceSection> sections, BigDecimal total) {
        this.title = title;
        this.date = date;
        this.header = List.copyOf(header);
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Identification of the recipient. */
    public List<String> getHeader() {
        return header;
    }

    /** Only sections that contain items. */
    public List<InvoiceSection> getSections() {
        return sections;
    }

    /** Sum of the amounts of every item in every section. */
    public BigDecimal getTotal() {
        return total;
    }
}
