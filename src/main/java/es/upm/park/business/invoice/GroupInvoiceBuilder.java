package es.upm.park.business.invoice;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;

import java.util.List;

/** Builds invoices for a group: identification and the group tickets it bought. */
public class GroupInvoiceBuilder extends InvoiceBuilder<GroupInvoiceBuilder> {

    public GroupInvoiceBuilder() {
        title("INVOICE - GROUP");
    }

    @Override
    protected GroupInvoiceBuilder self() {
        return this;
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        return recipient("Group", group.getId() + " - " + group.getName());
    }

    /**
     * One priced item per group ticket; its composition is descriptive detail and
     * is not added again to the total.
     */
    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        startSection("Group tickets");
        for (GroupTicket ticket : tickets) {
            List<String> composition = ticket.getComposition().stream()
                    .map(GroupInvoiceBuilder::describe)
                    .toList();
            item(ticket.getId() + " (" + ticket.getDate() + ", " + composition.size() + " members)",
                    ticket.getPrice(), composition);
        }
        return this;
    }

    private static String describe(IndividualTicket ticket) {
        return ticket.getId() + " " + ticket.getOwnerEmail() + " (included)";
    }
}
