package com.parque.business.service;

import com.parque.business.model.IndividualTicket;
import com.parque.business.model.InvoiceLine;
import com.parque.business.model.Pass;
import com.parque.business.model.SpeedPass;
import com.parque.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user's invoice from the individual tickets purchased directly for that user (constituents of group
 * tickets are not part of {@code directTickets}) and the personal passes attached to any of the user's tickets.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {
    private final User user;
    private final List<IndividualTicket> directTickets;
    private final List<Pass> passes;

    public UserInvoiceBuilder(User user, List<IndividualTicket> directTickets, List<Pass> passes) {
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
        this.passes = List.copyOf(passes);
    }

    @Override
    public void buildRecipient() {
        setRecipient("user " + user.getName() + " " + user.getSurname() + " <" + user.getEmail() + ">");
    }

    @Override
    public void buildIndividualTicketsSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket t : directTickets) {
            lines.add(new InvoiceLine(t.getId() + " (" + t.getDate() + ")", t.getPrice()));
        }
        addSection("Individual tickets", lines);
    }

    @Override
    public void buildPassesSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass p : passes) {
            String kind = p instanceof SpeedPass ? "SpeedPass" : "Special Pass";
            lines.add(new InvoiceLine(kind + " " + p.getId() + " (ticket " + p.getTicket().getId() + ")", p.getPrice()));
        }
        addSection("Personal passes", lines);
    }
}
