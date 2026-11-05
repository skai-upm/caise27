package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common abstraction for building invoices step by step. Concrete builders add the steps
 * meaningful for their recipient type; sections without lines are omitted.
 *
 * @param <B> concrete builder type, for fluent chaining
 */
public abstract class InvoiceBuilder<B extends InvoiceBuilder<B>> {

    private final String title;
    private final List<String> recipient = new ArrayList<>();
    private final Map<String, List<InvoiceLine>> sections = new LinkedHashMap<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected abstract B self();

    /** Step: identifying information of the recipient. */
    protected B recipient(String... lines) {
        recipient.addAll(List.of(lines));
        return self();
    }

    /** Step: adds a section; nothing is added when it has no lines. */
    protected B section(String name, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.computeIfAbsent(name, k -> new ArrayList<>()).addAll(lines);
        }
        return self();
    }

    /** Final step: creates the invoice, totalling the counted lines. */
    public Invoice build() {
        BigDecimal total = sections.values().stream().flatMap(List::stream)
                .filter(InvoiceLine::counted).map(InvoiceLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Invoice(title, recipient, sections, total);
    }
}
