package es.upm.parque.business;

import java.util.Comparator;
import java.util.List;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.InvoiceItem;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Builds the invoice of a group: identification and the group tickets bought for it. Each group ticket is
 * charged once at its total price; its composition is shown only as descriptive detail.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    private final Group group;
    private final ParkController park;

    public GroupInvoiceBuilder(Group group, ParkController park) {
        super("Invoice for group " + group.getId());
        this.group = group;
        this.park = park;
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Group: " + group.getId());
        addRecipientLine("Name: " + group.getName());
    }

    @Override
    public void buildPurchases() {
        List<InvoiceItem> items = park.groupTicketsOf(group).stream()
                .sorted(Comparator.comparing(Ticket::getDate).thenComparing(Ticket::getId))
                .map(GroupInvoiceBuilder::item)
                .toList();
        addSection("Group tickets", items);
    }

    private static InvoiceItem item(GroupTicket ticket) {
        int size = ticket.getComposition().size();
        String description = "Group ticket " + ticket.getId() + " for " + ticket.getDate() + " (" + size
                + " tickets, discount " + GroupTicket.discountPercentFor(size) + "%)";
        List<String> details = ticket.getComposition().stream().map(GroupInvoiceBuilder::detail).toList();
        return new InvoiceItem(description, ticket.getPrice(), details);
    }

    private static String detail(IndividualTicket component) {
        User holder = component.getUser();
        return holder.getFirstName() + " " + holder.getSurnames() + " (" + holder.getDni() + ") - ticket "
                + component.getId() + ", " + component.getPrice().toPlainString() + " EUR";
    }
}
