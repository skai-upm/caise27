package es.upm.amusementpark.business.invoice;

/**
 * Runs the construction steps of any invoice builder in order.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.reset();
        builder.buildHeader();
        builder.buildRecipient();
        builder.buildItems();
        builder.buildTotal();
        return builder.getResult();
    }
}
