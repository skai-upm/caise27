package es.upm.park.business.invoice;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group's invoice: the group's identity and its group tickets.
 * Each ticket's composition is described but only the group ticket's
 * total is charged.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    static final String GROUP_TICKETS = "Group tickets";

    public GroupInvoiceBuilder() {
        super("GROUP INVOICE");
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        addRecipientLine("Group: " + group.getId());
        addRecipientLine("Name: " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder addGroupTicket(GroupTicket ticket) {
        List<String> composition = new ArrayList<>();
        for (IndividualTicket component : ticket.getComposition()) {
            composition.add(component.getId() + " - " + component.getUser().getFullName() + " ("
                    + component.getPrice().toPlainString() + ", " + component.getDiscount().getPercent()
                    + "% discount)");
        }
        addItem(GROUP_TICKETS, new InvoiceItem("Group ticket " + ticket.getId() + " for " + ticket.getDate()
                + " (" + ticket.getComposition().size() + " members)", ticket.getPrice(), composition));
        return this;
    }
}
