package es.upm.parque.business.controller;

import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.InvoiceSection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder of invoices. Each step adds one part of the invoice; concrete builders implement the steps that
 * apply to their kind of recipient and leave the others empty. {@link InvoiceDirector} runs the steps in order.
 */
public abstract class InvoiceBuilder {

    private LocalDate issueDate;
    private String recipient;
    private final List<InvoiceSection> sections = new ArrayList<>();

    /** Starts a new invoice issued on {@code date}. */
    public void reset(LocalDate date) {
        issueDate = date;
        recipient = null;
        sections.clear();
    }

    /** Sets who the invoice is addressed to. */
    public abstract void buildHeader();

    /** Adds the individual tickets bought by the recipient, if applicable. */
    public void buildIndividualTickets() {
        // not applicable unless a concrete builder overrides it
    }

    /** Adds the group tickets bought by the recipient, if applicable. */
    public void buildGroupTickets() {
        // not applicable unless a concrete builder overrides it
    }

    public Invoice getResult() {
        return new Invoice(recipient, issueDate, sections);
    }

    protected void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /** Adds a section only if it has lines, so the invoice shows only available data. */
    protected void addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
    }
}
