package es.upm.park.business.service;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.InvoiceLine;
import es.upm.park.business.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds a user's invoice from the standalone individual tickets. Constituent tickets
 * of group tickets are never given to this builder, so they cannot be charged twice.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final Collection<IndividualTicket> tickets;

    public UserInvoiceBuilder(User user, Collection<IndividualTicket> standaloneTickets) {
        this.user = user;
        this.tickets = standaloneTickets;
    }

    @Override
    public void buildHeader() {
        setRecipient("user " + user.getName() + " " + user.getSurnames() + " <" + user.getEmail() + ">");
    }

    @Override
    public void buildIndividualTickets() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            if (t.isOwnedBy(user)) {
                lines.add(new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice()));
            }
        }
        addSection("Individual tickets", lines);
    }

    @Override
    public void buildGroupTickets() {
        // Group tickets are charged to the group, not to its members.
    }
}
