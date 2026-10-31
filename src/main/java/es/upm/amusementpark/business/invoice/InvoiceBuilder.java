package es.upm.amusementpark.business.invoice;

import java.util.List;

/**
 * Builder abstraction for invoices: the recipient, each applicable section and
 * finally the invoice itself are produced in separate steps.
 */
public interface InvoiceBuilder {

    /** Sets who the invoice is addressed to. */
    InvoiceBuilder recipient(String type, String id, String name);

    /** Adds a section; a section without lines is ignored. */
    InvoiceBuilder section(String title, List<InvoiceLine> lines);

    /** Returns the assembled invoice with its total. */
    Invoice build();
}
