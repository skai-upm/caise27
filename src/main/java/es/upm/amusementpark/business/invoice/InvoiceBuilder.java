package es.upm.amusementpark.business.invoice;

/**
 * Builder abstraction for invoices. Each step adds one part of the invoice; a concrete builder adds
 * only the parts that make sense for its recipient, and sections without items are left out.
 */
public interface InvoiceBuilder {

    void reset();

    void buildHeader();

    void buildTicketItems();

    void buildPassItems();

    void buildTotal();

    Invoice getResult();
}
