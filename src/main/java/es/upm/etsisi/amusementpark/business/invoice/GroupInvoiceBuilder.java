package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.InvoiceLine;
import java.util.List;

/** Builds the invoice of a group: its group tickets with their discounted totals. */
public class GroupInvoiceBuilder extends AbstractInvoiceBuilder {

    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, List<GroupTicket> groupTickets) {
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
    }

    @Override
    protected String recipient() {
        return "GROUP " + group.getId() + " (" + group.getName() + ")";
    }

    /** The constituent tickets of the group tickets are not charged again. */
    @Override
    public void buildIndividualTickets() {
        // not applicable to a group invoice
    }

    @Override
    public void buildGroupTickets() {
        addSection("Group tickets", groupTickets.stream().map(t -> new InvoiceLine(
                t.getId() + " " + t.getDate() + " " + t.getTickets().size() + " members, "
                        + GroupTicket.discountFor(t.getTickets().size()) + "% discount", t.getPrice())).toList());
    }
}
