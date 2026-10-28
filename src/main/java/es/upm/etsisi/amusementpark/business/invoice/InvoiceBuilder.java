package es.upm.etsisi.amusementpark.business.invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Common abstraction for building invoices step by step. Concrete builders decide
 * which recipient data and sections are relevant; empty sections are omitted.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private List<String> recipient;
    private List<InvoiceSection> sections;

    protected InvoiceBuilder(String title) {
        this.title = title;
        reset();
    }

    /** Starts a new invoice. */
    public final void reset() {
        recipient = new ArrayList<>();
        sections = new ArrayList<>();
    }

    /** Adds the identifying information of the recipient. */
    public abstract void buildRecipient();

    /** Adds the sections relevant to the recipient. */
    public abstract void buildSections();

    public final Invoice getResult() {
        return new Invoice(title, recipient, sections);
    }

    protected final void addRecipientLine(String line) {
        recipient.add(line);
    }

    /** Adds a section only when it has items. */
    protected final void addSection(String sectionTitle, List<InvoiceItem> items) {
        if (!items.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, items));
        }
    }
}
