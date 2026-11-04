package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder abstraction for invoices. Each concrete builder decides how the
 * header and item sections of its recipient type are built; sections without
 * lines are never added.
 */
public abstract class InvoiceBuilder {

    private static final int DECIMALS = 2;

    private String title;
    private final List<InvoiceSection> sections = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO.setScale(DECIMALS);

    /** Step 1: identifies the recipient. */
    public abstract void buildHeader();

    /** Step 2: adds the ticket purchases that apply to the recipient, if any. */
    public abstract void buildTicketSection();

    /** Step 3: totals every line added so far. */
    public void buildTotal() {
        total = sections.stream().map(InvoiceSection::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(DECIMALS, RoundingMode.HALF_UP);
    }

    public Invoice getResult() {
        return new Invoice(title, sections, total);
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    /** Adds a section only if it has at least one line. */
    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }
}
