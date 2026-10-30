package es.upm.amusementpark.business.invoice;

import java.util.List;

/**
 * Builder that assembles an {@link Invoice} step by step: recipient, sections, build.
 */
public interface InvoiceBuilder {

    /** Starts a new invoice for the given recipient, discarding any previous state. */
    InvoiceBuilder forRecipient(String recipientType, String recipient);

    /** Adds a section; sections without lines are not included in the invoice. */
    InvoiceBuilder addSection(String title, List<InvoiceLine> lines);

    Invoice build();
}
