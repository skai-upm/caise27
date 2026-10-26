package es.upm.parque.business.controller;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassStatus;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds the invoice of one user: the individual tickets bought directly for that user and the personal passes
 * of all the user's individual tickets. Internal tickets of a group ticket are billed to the group, so they are
 * not listed here, but passes bought for them are.
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

    @Override
    public void buildPersonalPasses() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                addPassLines(it, lines);
            } else if (t instanceof GroupTicket gt) {
                gt.getComponents().forEach(component -> addPassLines(component, lines));
            }
        }
        addSection("Personal passes", lines);
    }

    private void addPassLines(IndividualTicket ticket, List<InvoiceLine> lines) {
        if (!ticket.isOwnedBy(user)) {
            return;
        }
        for (Pass p : ticket.getPasses()) {
            if (p.getStatus() != PassStatus.REFUNDED) {
                lines.add(new InvoiceLine(p.getTypeName() + " " + p.getId() + " (ticket " + ticket.getId() + ")",
                    p.getPrice()));
            }
        }
    }
}
