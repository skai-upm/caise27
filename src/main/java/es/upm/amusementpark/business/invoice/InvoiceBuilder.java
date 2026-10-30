package es.upm.amusementpark.business.invoice;

/**
 * Builder of invoices: an invoice is assembled through these differentiated steps, each concrete builder adding
 * only the parts relevant to what it invoices.
 */
public interface InvoiceBuilder {

    void reset();

    void buildHeader();

    void buildRecipient();

    void buildItems();

    void buildTotal();

    Invoice getResult();
}
