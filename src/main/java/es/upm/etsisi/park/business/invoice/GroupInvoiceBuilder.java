package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.GroupTicket;
import es.upm.etsisi.park.business.IndividualTicket;

import java.util.List;

/**
 * Builds group invoices: group identification and group tickets, each counted
 * once with its composition shown as detail.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    public GroupInvoiceBuilder forGroup(Group group) {
        title("GROUP INVOICE");
        headerLine("GROUP: " + group.getId());
        headerLine("NAME: " + group.getName());
        return this;
    }

    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        section("GROUP TICKETS");
        for (GroupTicket t : tickets) {
            item("GROUPTICKET " + t.getId() + " (" + t.getDate() + ", " + t.getComposition().size() + " members)",
                    t.getPrice());
            for (IndividualTicket c : t.getComposition()) {
                detail(c.getId() + " " + c.getUser().getEmail() + " " + c.getPrice() + " EUR");
            }
        }
        return this;
    }
}
