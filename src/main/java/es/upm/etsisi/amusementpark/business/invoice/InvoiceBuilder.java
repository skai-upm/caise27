package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Common construction steps of every invoice. Concrete builders add the steps
 * specific to their recipient; each step contributes a section only when it
 * has items, and {@link #build()} computes the total from the items shown.
 *
 * @param <B> concrete builder type, returned by every step for chaining
 */
public abstract class InvoiceBuilder<B extends InvoiceBuilder<B>> {

    private final String title;
    private final List<String> header = new ArrayList<>();
    private final List<Invoice.Section> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected abstract B self();

    protected B headerLine(String line) {
        header.add(line);
        return self();
    }

    protected B section(String sectionTitle, List<Invoice.Item> items) {
        if (!items.isEmpty()) {
            sections.add(new Invoice.Section(sectionTitle, List.copyOf(items)));
        }
        return self();
    }

    public Invoice build() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Invoice.Section section : sections) {
            for (Invoice.Item item : section.items()) {
                total = total.add(item.amount());
            }
        }
        return new Invoice(title, header, sections, total);
    }
}
