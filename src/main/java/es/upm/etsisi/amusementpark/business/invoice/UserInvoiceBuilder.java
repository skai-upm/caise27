package es.upm.etsisi.amusementpark.business.invoice;

import java.util.List;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Builds a user invoice: customer data, directly purchased individual tickets and the
 * user's personal passes (also those on tickets composing group tickets).
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public UserInvoiceBuilder() {
        super("USER INVOICE");
    }

    public UserInvoiceBuilder customer(User user) {
        addRecipientLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addRecipientLine("DNI: " + user.getDni());
        addRecipientLine("Email: " + user.getEmail());
        return this;
    }

    public UserInvoiceBuilder individualTickets(List<IndividualTicket> tickets) {
        addSection("Individual tickets", tickets.stream()
                .map(t -> new InvoiceLine("Ticket " + t.getIdentifier() + " (" + t.getDate() + ")", t.getPrice()))
                .toList());
        return this;
    }

    public UserInvoiceBuilder speedPasses(List<? extends Pass> passes) {
        addSection("SpeedPasses", passLines("SpeedPass", passes));
        return this;
    }

    public UserInvoiceBuilder specialPasses(List<? extends Pass> passes) {
        addSection("Special Passes", passLines("Special Pass", passes));
        return this;
    }

    private static List<InvoiceLine> passLines(String label, List<? extends Pass> passes) {
        return passes.stream()
                .map(p -> new InvoiceLine(label + " " + p.getIdentifier() + " for ticket "
                        + p.getTicket().getIdentifier() + (p.isConsumed() ? " (consumed)" : ""), p.getPrice()))
                .toList();
    }
}
