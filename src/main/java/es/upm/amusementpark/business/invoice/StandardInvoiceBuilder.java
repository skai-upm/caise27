package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Default {@link InvoiceBuilder}: totals every line of the added sections. */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String type;
    private String id;
    private String name;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder recipient(String type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
        return this;
    }

    @Override
    public InvoiceBuilder section(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
        return this;
    }

    @Override
    public Invoice build() {
        if (type == null) {
            throw new IllegalStateException("invoice recipient not set");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                total = total.add(line.amount());
            }
        }
        return new Invoice(type, id, name, sections, total.setScale(2, RoundingMode.HALF_UP));
    }
}
