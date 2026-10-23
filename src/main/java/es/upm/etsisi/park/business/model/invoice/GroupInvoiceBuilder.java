package es.upm.etsisi.park.business.model.invoice;

import java.util.List;

import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Money;

/**
 * Builds invoices for a group: one priced item per group ticket, with its
 * member tickets listed as non-priced details so they are not counted twice.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    public static final String GROUP_TICKETS = "Group tickets";

    public GroupInvoiceBuilder group(Group group) {
        recipientLine("Group: " + group.getId() + " - " + group.getName());
        recipientLine("Members: " + group.getMembers().size());
        return this;
    }

    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        section(GROUP_TICKETS);
        for (GroupTicket ticket : tickets) {
            item("Group ticket " + ticket.getId() + " (" + ticket.getDate() + "), " + ticket.getMembers().size()
                    + " member(s), " + ticket.getDiscountPercent() + "% discount", ticket.getPrice());
            for (IndividualTicket member : ticket.getMembers()) {
                detail(member.getId() + "  " + member.getUser().getEmail() + "  " + Money.format(member.getPrice()));
            }
        }
        return this;
    }
}
