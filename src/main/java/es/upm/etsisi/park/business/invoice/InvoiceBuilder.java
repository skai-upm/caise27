package es.upm.etsisi.park.business.invoice;

/**
 * Common Builder abstraction: an invoice is assembled in separate steps, then retrieved.
 */
public interface InvoiceBuilder {

    void buildHeader();

    void buildRecipient();

    void buildPurchases();

    Invoice getInvoice();
}
