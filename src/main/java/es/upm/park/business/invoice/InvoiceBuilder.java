package es.upm.park.business.invoice;

import java.time.LocalDate;

/**
 * Common abstraction for building invoices step by step. Concrete builders
 * add the steps that make sense for their recipient type.
 */
public interface InvoiceBuilder {

    /**
     * Starts a new invoice issued on the given date, discarding any
     * previous, unfinished one.
     */
    InvoiceBuilder start(LocalDate issueDate);

    Invoice build();
}
