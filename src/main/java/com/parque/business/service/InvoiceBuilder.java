package com.parque.business.service;

import com.parque.business.model.Invoice;
import com.parque.business.model.InvoiceLine;
import com.parque.business.model.InvoiceSection;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder abstraction for invoices. Each construction step adds one part; steps that do not apply to a
 * recipient type keep the default (do nothing), and sections without lines are never added.
 */
public abstract class InvoiceBuilder {
    private String recipient = "";
    private final List<InvoiceSection> sections = new ArrayList<>();

    /** Step 1: who the invoice is for. */
    public abstract void buildRecipient();

    /** Step 2: individual tickets purchased directly. Not applicable by default. */
    public void buildIndividualTicketsSection() {
    }

    /** Step 3: group ticket purchases. Not applicable by default. */
    public void buildGroupTicketsSection() {
    }

    /** Final step: the assembled invoice with the total of its sections. */
    public Invoice getInvoice() {
        return new Invoice(recipient, sections);
    }

    protected void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /** Adds a section only if it has lines, so empty sections are omitted. */
    protected void addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
    }
}
