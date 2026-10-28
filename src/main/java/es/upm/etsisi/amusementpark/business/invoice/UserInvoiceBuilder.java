package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.List;

/**
 * Builds a user invoice: identification and the user's personal purchases.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final List<IndividualTicket> directTickets;
    private final List<Pass> passes;

    /**
     * @param directTickets individual tickets bought directly for the user (not part of a group ticket)
     * @param passes        passes associated with any of the user's individual tickets
     */
    public UserInvoiceBuilder(User user, List<IndividualTicket> directTickets, List<Pass> passes) {
        super("USER");
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
        this.passes = List.copyOf(passes);
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("User: " + user.getEmail());
        addRecipientLine("DNI: " + user.getDni());
    }

    @Override
    public void buildSections() {
        buildIndividualTicketSection();
        buildPassSections();
    }

    private void buildIndividualTicketSection() {
        addSection("Individual tickets", directTickets.stream()
                .map(ticket -> new InvoiceItem("Ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                        ticket.getUnitPrice(), List.of()))
                .toList());
    }

    /** Personal passes, including those of tickets that are part of a group ticket. */
    private void buildPassSections() {
        addSection("SpeedPasses", passItems(SpeedPass.class));
        addSection("Special Pass", passItems(SpecialPass.class));
    }

    private List<InvoiceItem> passItems(Class<? extends Pass> type) {
        return passes.stream()
                .filter(type::isInstance)
                .map(pass -> new InvoiceItem("Pass " + pass.getIdentifier() + " for ticket "
                        + pass.getTicket().getIdentifier() + (pass.isConsumed() ? " (consumed)" : ""),
                        pass.getPrice(), List.of()))
                .toList();
    }
}
