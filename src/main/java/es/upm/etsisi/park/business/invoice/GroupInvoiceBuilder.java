package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group's invoice: the group and the group tickets it bought, each with its
 * composition shown as details (not added again to the total).
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder<GroupInvoiceBuilder> {

    @Override
    protected GroupInvoiceBuilder self() {
        return this;
    }

    public GroupInvoiceBuilder forGroup(Group group) {
        return title("INVOICE - GROUP")
                .recipientLine("Group: " + group.getId())
                .recipientLine("Name: " + group.getName());
    }

    public GroupInvoiceBuilder withGroupTickets(List<GroupTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            List<String> composition = new ArrayList<>();
            for (IndividualTicket member : ticket.getTickets()) {
                User user = member.getUser();
                composition.add(member.getId() + " - " + user.getFirstName() + " " + user.getSurnames()
                        + " (" + member.getPrice().toPlainString() + ")");
            }
            int discount = ticket.getTickets().get(0).getDiscountPercent();
            lines.add(new InvoiceLine("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                    + ticket.getTickets().size() + " members, " + discount + "% discount)", ticket.getPrice(), composition));
        }
        return section("Group tickets", lines);
    }
}
