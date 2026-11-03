package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Invoice;
import java.time.LocalDate;

/** Runs the construction steps of any {@link InvoiceBuilder} in a fixed order. */
public final class InvoiceDirector {

    private InvoiceDirector() {
    }

    public static Invoice construct(InvoiceBuilder builder, LocalDate issueDate) {
        builder.buildHeader(issueDate);
        builder.buildIndividualTickets();
        builder.buildGroupTickets();
        return builder.getResult();
    }
}
