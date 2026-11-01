package es.upm.amusementpark.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link InvoiceBuilder}. A new builder is needed for each invoice.
 */
public class SimpleInvoiceBuilder implements InvoiceBuilder {

    private String recipient = "";
    private final List<String> titles = new ArrayList<>();
    private final List<List<InvoiceLine>> lines = new ArrayList<>();

    @Override
    public InvoiceBuilder recipient(String kind, String id, String name) {
        recipient = kind + " " + id + " - " + name;
        return this;
    }

    @Override
    public InvoiceBuilder section(String title) {
        titles.add(title);
        lines.add(new ArrayList<>());
        return this;
    }

    @Override
    public InvoiceBuilder item(String description, BigDecimal amount) {
        if (lines.isEmpty()) {
            throw new IllegalStateException("An invoice item needs a section");
        }
        lines.get(lines.size() - 1).add(new InvoiceLine(description, amount));
        return this;
    }

    @Override
    public Invoice build() {
        List<InvoiceSection> sections = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < titles.size(); i++) {
            if (lines.get(i).isEmpty()) {
                continue;
            }
            sections.add(new InvoiceSection(titles.get(i), lines.get(i)));
            for (InvoiceLine line : lines.get(i)) {
                total = total.add(line.amount());
            }
        }
        return new Invoice(recipient, sections, total.setScale(2, RoundingMode.HALF_UP));
    }
}
