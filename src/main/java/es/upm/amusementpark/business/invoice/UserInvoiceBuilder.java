package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/** Builds invoices for a single user. */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public UserInvoiceBuilder() {
        super("USER INVOICE");
    }

    public UserInvoiceBuilder addUser(User user) {
        addSection("User", List.of(InvoiceLine.detail("Email: " + user.getEmail()),
                InvoiceLine.detail("DNI: " + user.getDni())));
        return this;
    }

    /** Individual tickets purchased directly for the user (not group-ticket components). */
    public UserInvoiceBuilder addIndividualTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(InvoiceLine.item("Ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                    ticket.getPrice()));
        }
        addSection("Individual tickets", lines);
        return this;
    }
}
