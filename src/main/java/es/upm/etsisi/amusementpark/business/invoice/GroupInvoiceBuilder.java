package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;

import java.util.ArrayList;
import java.util.List;

/** Builds a group's invoice from its group tickets, without their constituent tickets. */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, List<GroupTicket> groupTickets) {
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
    }

    @Override
    public void buildHeader() {
        setTitle("Invoice for group " + group.getId() + " (" + group.getName() + ")");
    }

    @Override
    public void buildTicketSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket t : groupTickets) {
            int members = t.getParts().size();
            lines.add(new InvoiceLine(t.getId() + " " + t.getDate() + " (" + members + " members, "
                    + GroupTicket.discountPercent(members) + "% discount)", t.getPrice()));
        }
        addSection("Group tickets", lines);
    }
}
