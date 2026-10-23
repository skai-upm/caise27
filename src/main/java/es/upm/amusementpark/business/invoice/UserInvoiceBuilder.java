package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the invoice of one user: identification and personal purchases.
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder<UserInvoiceBuilder> {

    public UserInvoiceBuilder() {
        super("User invoice");
    }

    @Override
    protected UserInvoiceBuilder self() {
        return this;
    }

    public UserInvoiceBuilder customer(User user) {
        return headerLine("Customer: " + user.getFirstName() + " " + user.getSurnames())
                .headerLine("DNI: " + user.getDni())
                .headerLine("Email: " + user.getEmail());
    }

    /** Tickets bought directly for the user (never the ones inside group tickets). */
    public UserInvoiceBuilder individualTickets(List<IndividualTicket> tickets) {
        List<InvoiceItem> items = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            items.add(new InvoiceItem("Ticket " + ticket.getId() + " for " + ticket.getDate(), ticket.getPrice()));
        }
        return section("Individual tickets", items);
    }

    /** Personal SpeedPasses, including those bought for the user's tickets inside group tickets. */
    public UserInvoiceBuilder speedPasses(List<SpeedPass> passes) {
        return section("SpeedPasses", passItems("SpeedPass", passes));
    }

    /** Personal Special Passes, including those bought for the user's tickets inside group tickets. */
    public UserInvoiceBuilder specialPasses(List<SpecialPass> passes) {
        return section("Special Passes", passItems("Special Pass", passes));
    }

    private static List<InvoiceItem> passItems(String kind, List<? extends Pass> passes) {
        List<InvoiceItem> items = new ArrayList<>();
        for (Pass pass : passes) {
            items.add(new InvoiceItem(kind + " " + pass.getId() + " for ticket " + pass.getTicket().getId()
                    + (pass.isConsumed() ? " (consumed)" : ""), pass.getPrice()));
        }
        return items;
    }
}
