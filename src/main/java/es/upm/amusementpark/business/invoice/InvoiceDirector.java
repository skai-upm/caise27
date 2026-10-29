package es.upm.amusementpark.business.invoice;

/**
 * Runs the invoice construction steps in order on any {@link InvoiceBuilder}.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.reset();
        builder.buildHeader();
        builder.buildTicketItems();
        builder.buildPassItems();
        builder.buildTotal();
        return builder.getResult();
    }
}
