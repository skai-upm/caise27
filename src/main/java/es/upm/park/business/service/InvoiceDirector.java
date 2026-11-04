package es.upm.park.business.service;

import es.upm.park.business.model.Invoice;

/** Runs the invoice construction steps in a fixed order on any {@link InvoiceBuilder}. */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.buildHeader();
        builder.buildIndividualTickets();
        builder.buildGroupTickets();
        return builder.getResult();
    }
}
