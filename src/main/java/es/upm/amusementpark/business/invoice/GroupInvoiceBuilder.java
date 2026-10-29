package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/** Builds invoices for a group. */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public GroupInvoiceBuilder() {
        super("GROUP INVOICE");
    }

    public GroupInvoiceBuilder addGroup(Group group) {
        addSection("Group", List.of(InvoiceLine.detail("Identifier: " + group.getIdentifier()),
                InvoiceLine.detail("Name: " + group.getName())));
        return this;
    }

    /**
     * Group tickets with their total price, each followed by its composition as
     * descriptive lines, so composed tickets are not charged twice.
     */
    public GroupInvoiceBuilder addGroupTickets(List<GroupTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            lines.add(InvoiceLine.item("Group ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ", "
                    + ticket.getComposition().size() + " members)", ticket.getPrice()));
            for (IndividualTicket component : ticket.getComposition()) {
                lines.add(InvoiceLine.detail(component.getIdentifier() + " " + component.getUser().getEmail()
                        + " " + component.getPrice() + " EUR"));
            }
        }
        addSection("Group tickets", lines);
        return this;
    }
}
