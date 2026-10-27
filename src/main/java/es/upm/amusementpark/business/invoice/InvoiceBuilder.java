package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common abstraction for building invoices step by step. A section is only
 * created when its first line is added, so invoices never contain empty
 * sections.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private final List<String> header = new ArrayList<>();
    private final Map<String, List<InvoiceLine>> sections = new LinkedHashMap<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected void addHeaderLine(String line) {
        header.add(line);
    }

    protected void addLine(String section, InvoiceLine line) {
        sections.computeIfAbsent(section, s -> new ArrayList<>()).add(line);
    }

    /** Builds the invoice; the total is the sum of the lines that count in it. */
    public Invoice build() {
        List<InvoiceSection> built = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<String, List<InvoiceLine>> entry : sections.entrySet()) {
            built.add(new InvoiceSection(entry.getKey(), entry.getValue()));
            for (InvoiceLine line : entry.getValue()) {
                if (line.countsInTotal()) {
                    total = total.add(line.amount());
                }
            }
        }
        return new Invoice(title, header, built, total);
    }
}
