package com.parque.business.service;

import com.parque.business.model.IndividualTicket;
import com.parque.business.model.InvoiceLine;
import com.parque.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user's invoice from the individual tickets purchased directly for that user
 * (constituents of group tickets are not part of {@code directTickets}).
 */
public class UserInvoiceBuilder extends InvoiceBuilder {
    private final User user;
    private final List<IndividualTicket> directTickets;

    public UserInvoiceBuilder(User user, List<IndividualTicket> directTickets) {
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
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
}
