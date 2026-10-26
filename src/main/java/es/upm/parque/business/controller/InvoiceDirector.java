package es.upm.parque.business.controller;

import es.upm.parque.business.model.Invoice;
import java.time.LocalDate;

/**
 * Runs the invoice construction steps in a fixed order on any {@link InvoiceBuilder}.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder, LocalDate issueDate) {
        builder.reset(issueDate);
        builder.buildHeader();
        builder.buildIndividualTickets();
        builder.buildGroupTickets();
        builder.buildPersonalPasses();
        return builder.getResult();
    }
}
