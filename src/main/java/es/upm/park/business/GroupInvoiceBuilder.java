package es.upm.park.business;

import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.Invoice;
import java.util.List;

/**
 * Builds group invoices: each group-ticket purchase is charged once with its discounted
 * amount; its constituent individual tickets are not charged again.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    @Override
    public void buildGroupTickets(List<GroupTicket> groupTickets) {
        addSection("Group tickets", groupTickets.stream()
                .map(t -> new Invoice.Line(t.getId() + " " + t.getDate() + " (" + t.getTickets().size()
                        + " tickets, " + GroupTicket.discountPercent(t.getTickets().size()) + "% discount)",
                        t.getPrice()))
                .toList());
    }
}
