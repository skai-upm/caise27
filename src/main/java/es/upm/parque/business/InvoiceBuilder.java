package es.upm.parque.business;

import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceItem;
import es.upm.parque.business.model.InvoiceSection;

/**
 * Builder pattern: common abstraction for constructing an invoice step by step. Concrete builders decide what
 * each step contributes for their recipient type; {@link InvoiceManager} directs the steps.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private final List<String> recipient = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    /** Step 1: identifying information of the invoiced recipient. */
    public abstract void buildRecipient();

    /** Step 2: purchases made by or for the recipient. */
    public abstract void buildPurchases();

    /** Step 3: personal passes; recipients without personal passes (groups) add nothing. */
    public void buildPersonalPasses() {
        // Nothing to add by default: passes are personal purchases and are not invoiced to groups.
    }

    /** The invoice built so far, with only the sections that were added. */
    public Invoice getResult() {
        return new Invoice(title, recipient, sections);
    }

    protected void addRecipientLine(String line) {
        recipient.add(line);
    }

    /** Adds a section only when it has items, so sections with no information are omitted. */
    protected void addSection(String heading, List<InvoiceItem> items) {
        if (!items.isEmpty()) {
            sections.add(new InvoiceSection(heading, items));
        }
    }
}
