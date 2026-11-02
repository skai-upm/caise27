package es.upm.etsisi.park.business.invoice;

/**
 * Runs the construction steps of any {@link InvoiceBuilder} in order.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.buildHeader();
        builder.buildRecipient();
        builder.buildPurchases();
        return builder.getInvoice();
    }
}
