package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

/**
 * Builds the invoice of a user: identification and personal purchases.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    static final String TICKETS = "Individual tickets";

    public UserInvoiceBuilder() {
        super("INVOICE - USER");
    }

    public UserInvoiceBuilder user(User user) {
        header("Name: " + user.getFirstName() + " " + user.getSurnames());
        header("DNI: " + user.getDni());
        header("Email: " + user.getEmail());
        return this;
    }

    /**
     * Adds an individual ticket bought directly for the user.
     */
    public UserInvoiceBuilder directTicket(IndividualTicket ticket) {
        charge(TICKETS, ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice());
        return this;
    }
}
