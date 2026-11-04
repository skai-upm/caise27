package es.upm.park.business.service;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.InvoiceLine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Builds a group's invoice from its group-ticket purchases (stored totals). */
public class GroupInvoiceBuilder extends InvoiceBuilder {

    private final Group group;
    private final Collection<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, Collection<GroupTicket> groupTickets) {
        this.group = group;
        this.groupTickets = groupTickets;
    }

    @Override
    public void buildHeader() {
        setRecipient("group " + group.getId() + " " + group.getName());
    }

    @Override
    public void buildIndividualTickets() {
        // A group buys only group tickets; members' own tickets belong to their invoices.
    }

    @Override
    public void buildGroupTickets() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket t : groupTickets) {
            if (t.belongsTo(group)) {
                int members = t.getConstituents().size();
                lines.add(new InvoiceLine(t.getId() + " " + t.getDate() + " (" + members + " members, "
                        + GroupTicket.discountPercent(members) + "% discount)", t.getPrice()));
            }
        }
        addSection("Group tickets", lines);
    }
}
