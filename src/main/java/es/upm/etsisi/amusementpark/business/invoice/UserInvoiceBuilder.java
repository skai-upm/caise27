package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.InvoiceLine;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/**
 * Builds the invoice of a user: the individual tickets bought directly for that user and the personal passes
 * of all the user's tickets (including constituent tickets of group tickets, which are not charged here).
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    private final User user;
    private final List<Ticket> directTickets;
    private final List<Pass> passes;

    /**
     * @param directTickets tickets bought directly for the user (never constituent tickets of group tickets)
     * @param passes        personal passes of any of the user's tickets
     */
    public UserInvoiceBuilder(User user, List<Ticket> directTickets, List<Pass> passes) {
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
        this.passes = List.copyOf(passes);
    }

    @Override
    protected String recipient() {
        return "USER " + user.getEmail() + " (" + user.getName() + " " + user.getSurname() + ")";
    }

    @Override
    public void buildIndividualTickets() {
        addSection("Individual tickets", directTickets.stream()
                .map(t -> new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice())).toList());
    }

    /** Group tickets are charged to the group, not to its members. */
    @Override
    public void buildGroupTickets() {
        // not applicable to a user invoice
    }

    @Override
    public void buildPersonalPasses() {
        addSection("Personal passes", passes.stream().map(p -> new InvoiceLine(
                p.getId() + " " + p.getType() + " (ticket " + p.getTicket().getId() + ")", p.getPrice())).toList());
    }
}
