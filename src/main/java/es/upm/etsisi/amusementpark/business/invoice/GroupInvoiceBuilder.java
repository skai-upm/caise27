package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds group invoices: group identification and group tickets with their composition.
 * Constituent tickets are shown as details and never added to the total again.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    public GroupInvoiceBuilder forGroup(Group group) {
        reset("INVOICE - GROUP");
        addHeaderLine("Group: " + group.getId());
        addHeaderLine("Name: " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder withGroupTickets(List<GroupTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            lines.add(InvoiceLine.charge("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                    + ticket.composition().size() + " members)", ticket.getPrice()));
            for (IndividualTicket member : ticket.composition()) {
                lines.add(InvoiceLine.detail(member.getId() + " " + member.getUser().getEmail(), member.getPrice()));
            }
        }
        addSection("GROUP TICKETS", lines);
        return this;
    }
}
