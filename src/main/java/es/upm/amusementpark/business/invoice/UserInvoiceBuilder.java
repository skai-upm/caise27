package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;
import java.util.List;

/**
 * Builds the invoice of a user.
 */
public class UserInvoiceBuilder extends InvoiceBuilder<UserInvoiceBuilder> {

    public UserInvoiceBuilder() {
        super("INVOICE - USER");
    }

    @Override
    protected UserInvoiceBuilder self() {
        return this;
    }

    public UserInvoiceBuilder forUser(User user) {
        return recipient("Name: " + user.getFirstName() + " " + user.getSurnames(),
                "DNI: " + user.getDni(), "Email: " + user.getEmail());
    }

    /** Individual tickets purchased directly for the user (never group ticket components). */
    public UserInvoiceBuilder directTickets(List<IndividualTicket> tickets) {
        return section("Individual tickets", tickets.stream()
                .map(t -> InvoiceLine.item(t.getIdentifier() + " (" + t.getDate() + ")", t.getPrice())).toList());
    }
}
