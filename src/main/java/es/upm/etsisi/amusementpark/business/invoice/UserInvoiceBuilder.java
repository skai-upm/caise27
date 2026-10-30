package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.List;

/**
 * Builds a user's invoice: identification, tickets bought directly for the user and personal passes.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public UserInvoiceBuilder() {
        super("INVOICE - USER");
    }

    public UserInvoiceBuilder forUser(User user) {
        addRecipientLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addRecipientLine("DNI: " + user.getDni());
        addRecipientLine("Email: " + user.getEmail());
        return this;
    }

    /**
     * Stand-alone individual tickets only; tickets inside group tickets are charged to the group.
     */
    public UserInvoiceBuilder withTickets(List<IndividualTicket> tickets) {
        addSection("Individual tickets", tickets.stream()
                .map(t -> new InvoiceItem("Ticket " + t.getId() + " (" + t.getDate() + ")", t.getPrice()))
                .toList(), true);
        return this;
    }

    public UserInvoiceBuilder withSpeedPasses(List<SpeedPass> passes) {
        addSection("SpeedPasses", passes.stream()
                .map(p -> new InvoiceItem("SpeedPass " + p.getId() + " for ticket " + p.getTicket().getId()
                        + (p.isConsumed() ? " [consumed]" : ""), p.getPrice()))
                .toList(), true);
        return this;
    }

    public UserInvoiceBuilder withSpecialPasses(List<SpecialPass> passes) {
        addSection("Special Passes", passes.stream()
                .map(p -> new InvoiceItem("Special Pass " + p.getId() + " for ticket " + p.getTicket().getId()
                        + (p.isConsumed() ? " [consumed]" : ""), p.getPrice()))
                .toList(), true);
        return this;
    }
}
