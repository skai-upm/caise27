package es.upm.parque.business.invoice;

import java.util.List;

/**
 * Builds an invoice in steps: the recipient first, then any applicable sections, then the result.
 */
public interface InvoiceBuilder {

    InvoiceBuilder start(String recipient);

    /** Adds a section; a section without lines is not included. */
    InvoiceBuilder addSection(String title, List<InvoiceLine> lines);

    /** Returns the invoice with the total of every included line. */
    Invoice build();
}
