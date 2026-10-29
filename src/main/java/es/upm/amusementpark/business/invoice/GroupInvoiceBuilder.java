package es.upm.amusementpark.business.invoice;

import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;

/**
 * Builds group invoices: group identification and the group tickets bought, each with its
 * composition as descriptive detail (composition prices are already part of the group ticket total).
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    static final String GROUP_TICKETS = "Group tickets";

    @Override
    public GroupInvoiceBuilder reset() {
        super.reset();
        return this;
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        setTitle("INVOICE - GROUP");
        addHeaderLine("Group: " + group.getId());
        addHeaderLine("Name: " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder groupTicket(GroupTicket ticket) {
        List<String> composition = new ArrayList<>();
        for (IndividualTicket member : ticket.getComposition()) {
            composition.add(member.getId() + " " + member.getUser().getFirstName() + " "
                    + member.getUser().getSurnames() + " (" + member.getPrice().toPlainString() + " EUR included)");
        }
        addItem(GROUP_TICKETS, new InvoiceItem("Group ticket " + ticket.getId() + " (" + ticket.getDate()
                + ", " + ticket.getComposition().size() + " members)", ticket.getPrice(), composition));
        return this;
    }
}
