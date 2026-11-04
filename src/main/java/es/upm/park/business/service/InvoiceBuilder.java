package es.upm.park.business.service;

import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.InvoiceLine;
import es.upm.park.business.model.InvoiceSection;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder abstraction for invoices: each step adds one part of the invoice, and
 * concrete builders contribute only the parts applicable to their recipient.
 */
public abstract class InvoiceBuilder {

    private String recipient = "";
    private final List<InvoiceSection> sections = new ArrayList<>();

    /** Step 1: identifies the recipient. */
    public abstract void buildHeader();

    /** Step 2: individual tickets purchased directly by the recipient. */
    public abstract void buildIndividualTickets();

    /** Step 3: group tickets purchased by the recipient. */
    public abstract void buildGroupTickets();

    public Invoice getResult() {
        return new Invoice(recipient, sections);
    }

    protected void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /** Adds a section, or nothing when there are no lines (empty sections are omitted). */
    protected void addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
    }
}
