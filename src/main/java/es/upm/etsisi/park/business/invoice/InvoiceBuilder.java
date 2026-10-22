package es.upm.etsisi.park.business.invoice;

/**
 * Common abstraction for building invoices step by step.
 */
public interface InvoiceBuilder {

    /** Creates the invoice from the steps applied so far. */
    Invoice build();
}
