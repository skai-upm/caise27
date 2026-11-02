package es.upm.etsisi.park.business.invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared assembly of invoice parts; concrete builders decide which parts apply.
 */
public abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private String title = "INVOICE";
    private final List<String> recipient = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void addRecipientLine(String line) {
        recipient.add(line);
    }

    /** Adds the section only when it has lines, so empty sections never appear. */
    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }

    @Override
    public Invoice getInvoice() {
        return new Invoice(title, recipient, sections);
    }
}
