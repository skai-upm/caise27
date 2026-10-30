package es.upm.etsisi.amusementpark.business.invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Common abstraction of the Builder pattern for invoices: concrete builders add the parts that make
 * sense for their recipient type through these shared steps, and {@link #build()} returns the product.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private final List<String> recipient = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    protected void addRecipientLine(String line) {
        recipient.add(line);
    }

    /**
     * Adds a section only when it has items, so empty sections never appear.
     */
    protected void addSection(String sectionTitle, List<InvoiceItem> items, boolean charged) {
        if (!items.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, items, charged));
        }
    }

    public Invoice build() {
        return new Invoice(title, recipient, sections);
    }
}
