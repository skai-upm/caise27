package es.upm.amusementpark.business.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import es.upm.amusementpark.business.OperationException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Directs the construction of user and group invoices from the purchases
 * known to the park.
 */
public class InvoiceController {

    static final String INDIVIDUAL_TICKETS = "Individual tickets";
    static final String GROUP_TICKETS = "Group tickets";

    private final Park park;
    private final Supplier<InvoiceBuilder> builders;

    public InvoiceController(Park park, Supplier<InvoiceBuilder> builders) {
        this.park = park;
        this.builders = builders;
    }

    /** Invoice of the tickets bought directly for a user; group-ticket constituents are not counted. */
    public Invoice userInvoice(String email) {
        User user = park.getUserManager().findByEmail(email);
        if (user == null) {
            throw new OperationException("user " + email + " does not exist");
        }
        List<InvoiceLine> tickets = new ArrayList<>();
        for (Ticket ticket : park.getTickets()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                tickets.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")",
                        ticket.getPrice()));
            }
        }
        return builders.get()
                .forRecipient("User " + user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")")
                .addSection(INDIVIDUAL_TICKETS, tickets)
                .build();
    }

    /** Invoice of the group tickets bought by a group, without their constituent tickets. */
    public Invoice groupInvoice(String groupId) {
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new OperationException("group " + groupId + " does not exist");
        }
        List<InvoiceLine> groupTickets = new ArrayList<>();
        for (Ticket ticket : park.getTickets()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTickets.add(new InvoiceLine("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                        + groupTicket.getTickets().size() + " members)", ticket.getPrice()));
            }
        }
        return builders.get()
                .forRecipient("Group " + group.getId() + " " + group.getName())
                .addSection(GROUP_TICKETS, groupTickets)
                .build();
    }
}
