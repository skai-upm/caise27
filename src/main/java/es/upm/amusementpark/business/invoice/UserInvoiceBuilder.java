package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.IndividualTicket;
import java.math.BigDecimal;

/**
 * Builder of user invoices: identifying data, tickets bought directly for the user and personal passes.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public static final String TICKETS = "Individual tickets";
    public static final String PASSES = "Passes";

    public UserInvoiceBuilder() {
        super("USER");
    }

    public UserInvoiceBuilder forUser(String email, String dni) {
        recipient("Email", email);
        if (dni != null) {
            recipient("DNI", dni);
        }
        return this;
    }

    /** Adds a ticket bought directly for the user (never a group-ticket component). */
    public UserInvoiceBuilder addIndividualTicket(IndividualTicket ticket) {
        if (ticket.getContainingGroupTicket() != null) {
            throw new IllegalArgumentException("Group ticket components are invoiced with their group ticket");
        }
        item(TICKETS, ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice());
        return this;
    }

    /** Adds a personal pass purchase (SpeedPass or Special Pass). */
    public UserInvoiceBuilder addPass(String description, BigDecimal price) {
        item(PASSES, description, price);
        return this;
    }
}
