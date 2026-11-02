package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.util.List;

/**
 * Builds user invoices: identification, directly purchased tickets and personal passes.
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    public UserInvoiceBuilder forUser(User user) {
        reset("INVOICE - USER");
        addHeaderLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addHeaderLine("DNI: " + user.getDni());
        addHeaderLine("Email: " + user.getEmail());
        return this;
    }

    public UserInvoiceBuilder withTickets(List<IndividualTicket> tickets) {
        addSection("TICKETS", tickets.stream()
                .map(t -> InvoiceLine.charge("Ticket " + t.getId() + " (" + t.getDate() + ")", t.getPrice()))
                .toList());
        return this;
    }

    public UserInvoiceBuilder withSpeedPasses(List<SpeedPass> passes) {
        addSection("SPEEDPASSES", passLines("SpeedPass", passes));
        return this;
    }

    public UserInvoiceBuilder withSpecialPasses(List<SpecialPass> passes) {
        addSection("SPECIAL PASSES", passLines("Special Pass", passes));
        return this;
    }

    private static List<InvoiceLine> passLines(String kind, List<? extends Pass> passes) {
        return passes.stream().map(p -> InvoiceLine.charge(kind + " " + p.getId() + " for ticket "
                + p.getTicket().getId() + (p.isConsumed() ? " (consumed)" : ""), p.getPrice())).toList();
    }
}
