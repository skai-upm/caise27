package es.upm.etsisi.amusementpark.business.invoice;

/**
 * Common abstraction for building invoices step by step.
 */
public interface InvoiceBuilder {

    /** Starts a new invoice, discarding any previous parts. */
    void reset(String title);

    void addHeaderLine(String line);

    Invoice build();
}
