package com.parque.business.service;

import com.parque.business.model.Group;
import com.parque.business.model.GroupTicket;
import com.parque.business.model.InvoiceLine;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a group's invoice from its group ticket purchases, charging each group ticket's total once and never
 * its constituent individual tickets.
 */
public class GroupInvoiceBuilder extends InvoiceBuilder {
    private final Group group;
    private final List<GroupTicket> groupTickets;

    public GroupInvoiceBuilder(Group group, List<GroupTicket> groupTickets) {
        this.group = group;
        this.groupTickets = List.copyOf(groupTickets);
    }

    @Override
    public void buildRecipient() {
        setRecipient("group " + group.getId() + " - " + group.getName());
    }

    @Override
    public void buildGroupTicketsSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket t : groupTickets) {
            lines.add(new InvoiceLine(t.getId() + " (" + t.getDate() + ", " + t.getConstituents().size() + " members)",
                    t.getPrice()));
        }
        addSection("Group tickets", lines);
    }
}
