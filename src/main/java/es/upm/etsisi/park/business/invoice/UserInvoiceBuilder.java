package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user's invoice: identification, tickets bought directly for the user and the
 * personal passes of any of the user's individual tickets.
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder<UserInvoiceBuilder> {

    @Override
    protected UserInvoiceBuilder self() {
        return this;
    }

    public UserInvoiceBuilder forUser(User user) {
        return title("INVOICE - USER")
                .recipientLine("Name: " + user.getFirstName() + " " + user.getSurnames())
                .recipientLine("DNI: " + user.getDni())
                .recipientLine("Email: " + user.getEmail());
    }

    public UserInvoiceBuilder withIndividualTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        }
        return section("Individual tickets", lines);
    }

    public UserInvoiceBuilder withSpeedPasses(List<SpeedPass> passes) {
        return section("SpeedPasses", passLines(passes));
    }

    public UserInvoiceBuilder withSpecialPasses(List<SpecialPass> passes) {
        return section("Special Passes", passLines(passes));
    }

    private static List<InvoiceLine> passLines(List<? extends Pass> passes) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(new InvoiceLine(pass.getKind() + " " + pass.getId() + " for ticket " + pass.getTicket().getId()
                    + (pass.isConsumed() ? " (consumed)" : ""), pass.getPrice()));
        }
        return lines;
    }
}
