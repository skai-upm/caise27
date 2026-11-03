package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An invoice produced by an {@link InvoiceBuilder}. It only contains the
 * sections that apply to its recipient.
 */
public final class Invoice {

    /** A charged item; details describe it without adding to the total. */
    public record Item(String description, BigDecimal amount, List<String> details) {

        public Item(String description, BigDecimal amount) {
            this(description, amount, List.of());
        }
    }

    /** A titled group of items. */
    public record Section(String title, List<Item> items) {
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
}
