package es.upm.parque.business.controller;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.InvoiceLine;
import es.upm.parque.business.model.Ticket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds the invoice of one group: its group tickets with their discounted totals. The internal tickets and the
 * members' own purchases are not listed again.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    private final Group group;
    private final Collection<Ticket> tickets;

    /** @param tickets the top-level tickets of the park */
    public GroupInvoiceBuilder(Group group, Collection<Ticket> tickets) {
        this.group = group;
        this.tickets = tickets;
    }

    @Override
    public void buildHeader() {
        setRecipient("Group " + group.getId() + " " + group.getName());
    }

    @Override
    public void buildGroupTickets() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.isForGroup(group)) {
                lines.add(new InvoiceLine("Group ticket " + gt.getId() + " for " + gt.getDate() + " ("
                    + gt.getComponents().size() + " members, " + gt.getDiscountPercent() + "% discount)",
                    gt.getPrice()));
            }
        }
        addSection("Group tickets", lines);
    }
}
