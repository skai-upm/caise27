package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Pass;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user's invoice: identifying data, directly purchased tickets and
 * personal passes.
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
        return headerLine("Name: " + user.getFirstName() + " " + user.getSurnames())
                .headerLine("DNI: " + user.getDni())
                .headerLine("Email: " + user.getEmail());
    }

    /** Tickets bought directly for the user; composed group tickets must not be passed here. */
    public UserInvoiceBuilder individualTickets(List<IndividualTicket> tickets) {
        List<Invoice.Item> items = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            items.add(new Invoice.Item("Ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                    ticket.getPrice()));
        }
        return section("Individual tickets", items);
    }

    public UserInvoiceBuilder speedPasses(List<? extends Pass> passes) {
        return section("SpeedPasses", passItems(passes));
    }

    public UserInvoiceBuilder specialPasses(List<? extends Pass> passes) {
        return section("Special Passes", passItems(passes));
    }

    private static List<Invoice.Item> passItems(List<? extends Pass> passes) {
        List<Invoice.Item> items = new ArrayList<>();
        for (Pass pass : passes) {
            items.add(new Invoice.Item("Pass " + pass.getIdentifier() + " (ticket " + pass.getTicket().getIdentifier()
                    + (pass.isConsumed() ? ", consumed" : "") + ")", pass.getPrice()));
        }
        return items;
    }
}
