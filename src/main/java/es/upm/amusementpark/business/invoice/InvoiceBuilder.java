package es.upm.amusementpark.business.invoice;

import java.util.List;

/**
 * Builds an {@link Invoice} step by step: the recipient first, then each
 * applicable section, and finally {@link #build()}.
 */
public interface InvoiceBuilder {

    InvoiceBuilder forRecipient(String recipient);

    /** Adds a section; sections without lines are not included in the invoice. */
    InvoiceBuilder addSection(String title, List<InvoiceLine> lines);

    Invoice build();
}
