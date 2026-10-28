package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.List;

/**
 * Builds a user invoice: identification and the user's personal purchases.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final List<IndividualTicket> directTickets;

    /**
     * @param directTickets individual tickets bought directly for the user (not part of a group ticket)
     */
    public UserInvoiceBuilder(User user, List<IndividualTicket> directTickets) {
        super("USER");
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
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

    /** Personal passes of the user's tickets; the park offers no passes yet. */
    private void buildPassSections() {
        // no pass products exist yet
    }
}
