package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common abstraction for building invoices step by step. Concrete builders expose the steps meaningful to their
 * recipient type; empty sections are never produced.
 */
public abstract class InvoiceBuilder {

    private final String type;
    private final Map<String, String> recipient = new LinkedHashMap<>();
    private final Map<String, List<Invoice.Line>> sections = new LinkedHashMap<>();

    protected InvoiceBuilder(String type) {
        this.type = type;
    }

    /** Step: adds a piece of identifying information of the recipient. */
    protected void recipient(String label, String value) {
        recipient.put(label, value);
    }

    /** Step: adds an item counted in the total to a section. */
    protected void item(String section, String text, BigDecimal amount) {
        sections.computeIfAbsent(section, s -> new ArrayList<>()).add(new Invoice.Line(text, amount));
    }

    /** Step: adds an informational line (never counted) to a section. */
    protected void detail(String section, String text) {
        sections.computeIfAbsent(section, s -> new ArrayList<>()).add(new Invoice.Line(text, null));
    }

    /**
     * Final step: creates the invoice with the sections that received lines and the total of their items.
     *
     * @return the invoice
     */
    public Invoice build() {
        List<Invoice.Section> result = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<String, List<Invoice.Line>> entry : sections.entrySet()) {
            result.add(new Invoice.Section(entry.getKey(), List.copyOf(entry.getValue())));
            for (Invoice.Line line : entry.getValue()) {
                if (line.isItem()) {
                    total = total.add(line.amount());
                }
            }
        }
        return new Invoice(type, Collections.unmodifiableMap(new LinkedHashMap<>(recipient)), List.copyOf(result),
                total);
    }
}
