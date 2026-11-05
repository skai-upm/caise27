package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the invoice of a group.
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
        return recipient("Group: " + group.getIdentifier(), "Name: " + group.getName());
    }

    /** Group tickets with their composition as non-counted detail. */
    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket g : tickets) {
            lines.add(InvoiceLine.item(g.getIdentifier() + " (" + g.getDate() + ", "
                    + g.getComposition().size() + " members)", g.getPrice()));
            for (IndividualTicket c : g.getComposition()) {
                lines.add(InvoiceLine.detail(c.getIdentifier() + " - " + c.getUser().getEmail()
                        + " (" + Invoice.money(c.getPrice()) + ", included)"));
            }
        }
        return section("Group tickets", lines);
    }
}
