package es.upm.parque.business.controller;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds the invoice of one user: the individual tickets bought directly for that user. Internal tickets of a
 * group ticket are billed to the group, so they are not listed here.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final Collection<Ticket> tickets;

    /** @param tickets the top-level tickets of the park (internal tickets are not top-level) */
    public UserInvoiceBuilder(User user, Collection<Ticket> tickets) {
        this.user = user;
        this.tickets = tickets;
    }

    @Override
    public void buildHeader() {
        setRecipient("User " + user.getName() + " " + user.getSurname() + " <" + user.getEmail() + "> DNI "
            + user.getDni());
    }

    @Override
    public void buildIndividualTickets() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it && it.isOwnedBy(user)) {
                lines.add(new InvoiceLine("Ticket " + it.getId() + " for " + it.getDate(), it.getPrice()));
            }
        }
        addSection("Individual tickets", lines);
    }
}
