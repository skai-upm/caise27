package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Invoice;
import java.time.LocalDate;

/**
 * Builder abstraction for invoices. Each step adds the part of the invoice it is responsible for;
 * a concrete builder adds nothing in the steps that do not apply to its kind of recipient.
 */
public interface InvoiceBuilder {

    /** Starts a new invoice for the builder's recipient, issued on the given date. */
    void buildHeader(LocalDate issueDate);

    /** Adds the individual tickets purchased directly by the recipient, if any. */
    void buildIndividualTickets();

    /** Adds the group tickets purchased by the recipient, if any. */
    void buildGroupTickets();

    /** Adds the personal passes of the recipient's tickets, if any. */
    void buildPersonalPasses();

    /** Returns the invoice assembled by the previous steps. */
    Invoice getResult();
}
