package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the generic building steps shared by every invoice type.
 */
public abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private String title = "INVOICE";
    private final List<String> header = new ArrayList<>();
    private final List<Invoice.Section> sections = new ArrayList<>();
    private List<Invoice.Line> currentLines;
    private BigDecimal total = BigDecimal.ZERO.setScale(2);

    @Override
    public InvoiceBuilder title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public InvoiceBuilder headerLine(String line) {
        header.add(line);
        return this;
    }

    @Override
    public InvoiceBuilder section(String title) {
        currentLines = new ArrayList<>();
        sections.add(new Invoice.Section(title, currentLines));
        return this;
    }

    @Override
    public InvoiceBuilder item(String text, BigDecimal amount) {
        requireSection().add(new Invoice.Line(text, amount));
        total = total.add(amount);
        return this;
    }

    @Override
    public InvoiceBuilder detail(String text) {
        requireSection().add(new Invoice.Line(text, null));
        return this;
    }

    @Override
    public Invoice build() {
        List<Invoice.Section> filled = new ArrayList<>();
        for (Invoice.Section s : sections) {
            if (!s.lines().isEmpty()) {
                filled.add(new Invoice.Section(s.title(), List.copyOf(s.lines())));
            }
        }
        return new Invoice(title, header, filled, total);
    }

    private List<Invoice.Line> requireSection() {
        if (currentLines == null) {
            section("ITEMS");
        }
        return currentLines;
    }
}
