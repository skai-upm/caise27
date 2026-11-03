package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.InvoiceLine;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/** Builds the invoice of a user: the individual tickets bought directly for that user. */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    private final User user;
    private final List<Ticket> directTickets;

    /** @param directTickets tickets bought directly for the user (never constituent tickets of group tickets) */
    public UserInvoiceBuilder(User user, List<Ticket> directTickets) {
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
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
}
