package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * State and steps shared by the concrete invoice builders.
 */
abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private String title;
    private final List<String> header = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();
    private BigDecimal total;

    @Override
    public void reset() {
        title = "";
        header.clear();
        sections.clear();
        total = null;
    }

    @Override
    public void buildTotal() {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                sum = sum.add(line.amount());
            }
        }
        total = sum;
    }

    @Override
    public Invoice getResult() {
        if (total == null) {
            buildTotal();
        }
        return new Invoice(title, header, sections, total);
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void addHeaderLine(String line) {
        header.add(line);
    }

    /**
     * Adds a section only if it has items, so empty sections never appear.
     */
    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }
}
