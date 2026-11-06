package es.upm.park.business.invoice;

import es.upm.park.business.model.IndividualTicket;

import java.util.List;

/** Builds invoices for a user: identification and the user's direct purchases. */
public class UserInvoiceBuilder extends InvoiceBuilder<UserInvoiceBuilder> {

    public UserInvoiceBuilder() {
        title("INVOICE - USER");
    }

    @Override
    protected UserInvoiceBuilder self() {
        return this;
    }

    public UserInvoiceBuilder forUser(String email, String dni) {
        return recipient("User", email).recipient("DNI", dni);
    }

    /** Individual tickets bought directly; tickets composing a group ticket are not the user's purchase. */
    public UserInvoiceBuilder directTickets(List<IndividualTicket> tickets) {
        startSection("Individual tickets");
        tickets.stream()
                .filter(ticket -> !ticket.isPartOfGroupTicket())
                .forEach(ticket -> item(ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        return this;
    }
}
