package es.upm.etsisi.amusementpark.business.invoice;

/** Runs the invoice construction steps in order for any {@link InvoiceBuilder}. */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.buildHeader();
        builder.buildTicketSection();
        builder.buildTotal();
        return builder.getResult();
    }
}
