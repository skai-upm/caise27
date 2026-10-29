package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common abstraction for building invoices step by step. Concrete builders add the parts that make
 * sense for their recipient; {@link #build()} keeps only non-empty sections and totals the items shown.
 */
public abstract class InvoiceBuilder {

    private String title;
    private final List<String> header = new ArrayList<>();
    private final Map<String, List<InvoiceItem>> sections = new LinkedHashMap<>();

    /** Starts a new invoice, discarding any previous state. */
    public InvoiceBuilder reset() {
        title = null;
        header.clear();
        sections.clear();
        return this;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void addHeaderLine(String line) {
        header.add(line);
    }

    protected void addItem(String section, InvoiceItem item) {
        sections.computeIfAbsent(section, s -> new ArrayList<>()).add(item);
    }

    public Invoice build() {
        if (title == null) {
            throw new IllegalStateException("invoice recipient not set");
        }
        List<InvoiceSection> built = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<String, List<InvoiceItem>> e : sections.entrySet()) {
            if (e.getValue().isEmpty()) {
                continue;
            }
            built.add(new InvoiceSection(e.getKey(), e.getValue()));
            for (InvoiceItem item : e.getValue()) {
                total = total.add(item.amount());
            }
        }
        return new Invoice(title, header, built, total);
    }
}
