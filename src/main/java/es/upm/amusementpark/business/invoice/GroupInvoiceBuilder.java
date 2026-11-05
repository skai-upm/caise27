package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.Group;
import es.upm.amusementpark.business.GroupTicket;
import es.upm.amusementpark.business.IndividualTicket;

/**
 * Builder of group invoices: group identity and group tickets with their composition as detail.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public static final String GROUP_TICKETS = "Group tickets";

    public GroupInvoiceBuilder() {
        super("GROUP");
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        recipient("Identifier", group.getId());
        recipient("Name", group.getName());
        return this;
    }

    /** Adds a group ticket (its total is counted once; components are informational detail). */
    public GroupInvoiceBuilder addGroupTicket(GroupTicket ticket) {
        item(GROUP_TICKETS, ticket.getId() + " (" + ticket.getDate() + ", " + ticket.getComponents().size()
                + " members)", ticket.getPrice());
        for (IndividualTicket component : ticket.getComponents()) {
            detail(GROUP_TICKETS, component.getId() + " " + component.getUserEmail() + " "
                    + component.getPrice().toPlainString());
        }
        return this;
    }
}
