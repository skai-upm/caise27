package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.controller.SystemController;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the invoice of a group: identification and the group tickets it
 * bought, each described by its composition.
 */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder<GroupInvoiceBuilder> {

    public GroupInvoiceBuilder() {
        super("Group invoice");
    }

    @Override
    protected GroupInvoiceBuilder self() {
        return this;
    }

    public GroupInvoiceBuilder group(Group group) {
        return headerLine("Group: " + group.getId() + " - " + group.getName());
    }

    /** One charged item per group ticket; its individual tickets are listed as details only. */
    public GroupInvoiceBuilder groupTickets(List<GroupTicket> tickets) {
        List<InvoiceItem> items = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            List<String> details = new ArrayList<>();
            for (IndividualTicket member : ticket.getComposition()) {
                details.add(member.getId() + " " + member.getUser().getFirstName() + " "
                        + member.getUser().getSurnames() + " (" + SystemController.formatAmount(member.getPrice())
                        + " EUR)");
            }
            items.add(new InvoiceItem("Group ticket " + ticket.getId() + " for " + ticket.getDate() + ", "
                    + ticket.getComposition().size() + " tickets, " + ticket.getDiscountPercent() + "% discount",
                    ticket.getPrice(), details));
        }
        return section("Group tickets", items);
    }
}
