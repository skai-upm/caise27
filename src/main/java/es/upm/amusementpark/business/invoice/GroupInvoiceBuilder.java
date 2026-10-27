package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the invoice of a group: identification and the group tickets it
 * purchased, with their composition shown as details.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    static final String GROUP_TICKETS = "Group tickets";

    public GroupInvoiceBuilder() {
        super("INVOICE - GROUP");
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        addHeaderLine("Group: " + group.getIdentifier() + " - " + group.getName());
        return this;
    }

    /** Adds a group ticket; its composed tickets are details, not extra charges. */
    public GroupInvoiceBuilder addGroupTicket(GroupTicket ticket) {
        List<String> composition = new ArrayList<>();
        for (IndividualTicket composed : ticket.getComposition()) {
            composition.add(composed.getIdentifier() + " - " + composed.getUser().getEmail() + " ("
                    + composed.getPrice() + " EUR)");
        }
        addLine(GROUP_TICKETS, new InvoiceLine(ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                ticket.getPrice(), composition, true));
        return this;
    }
}
