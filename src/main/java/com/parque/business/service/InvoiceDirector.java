package com.parque.business.service;

import com.parque.business.model.Invoice;

/**
 * Director of the Builder pattern: runs the invoice construction steps in a fixed order on any builder.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.buildRecipient();
        builder.buildIndividualTicketsSection();
        builder.buildGroupTicketsSection();
        builder.buildPassesSection();
        return builder.getInvoice();
    }
}
