package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

/**
 * Builds the invoice of a user: identification and the user's personal
 * purchases.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    static final String TICKETS = "Individual tickets";

    public UserInvoiceBuilder() {
        super("INVOICE - USER");
    }

    public UserInvoiceBuilder forUser(User user) {
        addHeaderLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addHeaderLine("DNI: " + user.getDni());
        addHeaderLine("Email: " + user.getEmail());
        return this;
    }

    /** Adds a ticket bought directly for the user. */
    public UserInvoiceBuilder addIndividualTicket(IndividualTicket ticket) {
        addLine(TICKETS, InvoiceLine.charged(ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                ticket.getPrice()));
        return this;
    }
}
