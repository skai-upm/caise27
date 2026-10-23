package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Steps shared by every invoice builder: date, header lines, non-empty
 * sections and the total.
 *
 * @param <B> the concrete builder
 */
public abstract class AbstractInvoiceBuilder<B extends AbstractInvoiceBuilder<B>> implements InvoiceBuilder<B> {

    private final String title;
    private final List<String> header = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();
    private LocalDate date;

    protected AbstractInvoiceBuilder(String title) {
        this.title = title;
    }

    /** Returns this builder with its concrete type. */
    protected abstract B self();

    protected B headerLine(String line) {
        header.add(line);
        return self();
    }

    @Override
    public B issuedOn(LocalDate issueDate) {
        this.date = issueDate;
        return self();
    }

    @Override
    public B section(String sectionTitle, List<InvoiceItem> items) {
        if (!items.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, items));
        }
        return self();
    }

    /**
     * @throws IllegalStateException if the date or the recipient was not set
     */
    @Override
    public Invoice build() {
        if (date == null || header.isEmpty()) {
            throw new IllegalStateException("An invoice needs an issue date and a recipient");
        }
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            for (InvoiceItem item : section.items()) {
                total = total.add(item.amount());
            }
        }
        return new Invoice(title, date, header, sections, total);
    }
}
