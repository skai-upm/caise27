package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group's invoice: group data and its group tickets, each counted once
 * with its composition shown as detail.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder<GroupInvoiceBuilder> {

    public GroupInvoiceBuilder() {
        super("INVOICE - GROUP");
    }

    @Override
    protected GroupInvoiceBuilder self() {
        return this;
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        return headerLine("Group: " + group.getIdentifier() + " - " + group.getName());
    }

    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        List<Invoice.Item> items = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            List<String> composition = new ArrayList<>();
            for (IndividualTicket member : ticket.getComposition()) {
                composition.add(member.getIdentifier() + " " + member.getUser().getEmail() + " "
                        + member.getPrice().toPlainString() + " EUR (included)");
            }
            items.add(new Invoice.Item("Group ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ", "
                    + composition.size() + " members)", ticket.getPrice(), composition));
        }
        return section("Group tickets", items);
    }
}
