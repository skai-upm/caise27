package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common abstraction for building invoices step by step. Sections are created on demand when
 * their first item is added, so sections without information never appear.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private final List<String> header = new ArrayList<>();
    private final Map<String, List<InvoiceItem>> sections = new LinkedHashMap<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected void header(String line) {
        header.add(line);
    }

    protected void charge(String section, String description, BigDecimal amount) {
        items(section).add(InvoiceItem.charged(description, amount));
    }

    protected void detail(String section, String description) {
        items(section).add(InvoiceItem.detail(description));
    }

    private List<InvoiceItem> items(String section) {
        return sections.computeIfAbsent(section, s -> new ArrayList<>());
    }

    /**
     * Final step: creates the invoice with the sections that received items and the sum of every
     * charged item.
     */
    public Invoice build() {
        List<InvoiceSection> built = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<String, List<InvoiceItem>> e : sections.entrySet()) {
            built.add(new InvoiceSection(e.getKey(), e.getValue()));
            for (InvoiceItem item : e.getValue()) {
                total = total.add(item.getAmount().orElse(BigDecimal.ZERO));
            }
        }
        return new Invoice(title, header, built, total);
    }
}
