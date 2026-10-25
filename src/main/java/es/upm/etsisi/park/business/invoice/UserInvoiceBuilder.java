package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user's invoice: identification, directly bought tickets and personal passes.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public UserInvoiceBuilder() {
        super("USER INVOICE");
    }

    public UserInvoiceBuilder forUser(User user) {
        addRecipientField("Customer", user.getFirstName() + " " + user.getSurnames());
        addRecipientField("DNI", user.getDni());
        addRecipientField("Email", user.getEmail());
        return this;
    }

    public UserInvoiceBuilder withIndividualTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(InvoiceLine.priced("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        }
        addSection("Individual tickets", lines);
        return this;
    }

    public UserInvoiceBuilder withSpeedPasses(List<? extends Pass> passes) {
        addSection("SpeedPasses", passLines(passes));
        return this;
    }

    public UserInvoiceBuilder withSpecialPasses(List<? extends Pass> passes) {
        addSection("Special Passes", passLines(passes));
        return this;
    }

    private static List<InvoiceLine> passLines(List<? extends Pass> passes) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(InvoiceLine.priced(pass.getId() + " for ticket " + pass.getTicket().getId()
                    + (pass.isConsumed() ? " (consumed)" : ""), pass.getPrice()));
        }
        return lines;
    }
}
