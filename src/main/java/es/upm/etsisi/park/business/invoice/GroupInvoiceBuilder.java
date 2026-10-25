package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group's invoice: identification and group tickets with their composition.
 * Component tickets are informational lines, so they are never added to the total twice.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public GroupInvoiceBuilder() {
        super("GROUP INVOICE");
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        addRecipientField("Group", group.getId());
        addRecipientField("Name", group.getName());
        return this;
    }

    public GroupInvoiceBuilder withGroupTickets(List<GroupTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            lines.add(InvoiceLine.priced("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                    + ticket.getTickets().size() + " members, " + ticket.getDiscount().getPercent() + "% discount)",
                    ticket.getPrice()));
            for (IndividualTicket component : ticket.getTickets()) {
                lines.add(InvoiceLine.informational(component.getId() + " " + component.getUser().getEmail() + " "
                        + component.getPrice().toPlainString() + " EUR"));
            }
        }
        addSection("Group tickets", lines);
        return this;
    }
}
