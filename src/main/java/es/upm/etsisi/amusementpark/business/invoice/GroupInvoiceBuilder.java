package es.upm.etsisi.amusementpark.business.invoice;

import java.util.List;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;

/**
 * Builds a group invoice: group data and its group tickets, each charged once with its
 * composition listed as detail. Members' personal passes are not included.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public GroupInvoiceBuilder() {
        super("GROUP INVOICE");
    }

    public GroupInvoiceBuilder group(Group group) {
        addRecipientLine("Group: " + group.getIdentifier() + " - " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        addSection("Group tickets", tickets.stream()
                .map(t -> new InvoiceLine("Group ticket " + t.getIdentifier() + " (" + t.getDate() + ", "
                        + t.getComposition().size() + " members)", t.getPrice(),
                        t.getComposition().stream()
                                .map(c -> c.getIdentifier() + " " + c.getUser().getEmail() + " "
                                        + c.getPrice().toPlainString() + " EUR")
                                .toList()))
                .toList());
        return this;
    }
}
