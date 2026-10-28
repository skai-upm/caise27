package es.upm.etsisi.amusementpark.business.invoice;

/**
 * Runs the construction steps of any invoice builder in order.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder) {
        builder.reset();
        builder.buildRecipient();
        builder.buildSections();
        return builder.getResult();
    }
}
