package es.upm.amusementpark.business.controller;

import java.util.function.Supplier;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Invoice;
import es.upm.amusementpark.business.model.InvoiceBuilder;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Directs invoice construction: decides which sections apply to the recipient
 * and feeds them to a fresh builder.
 */
public class InvoiceController {

    private final TicketController tickets;
    private final Supplier<InvoiceBuilder> builders;

    public InvoiceController(TicketController tickets, Supplier<InvoiceBuilder> builders) {
        this.tickets = tickets;
        this.builders = builders;
    }

    /**
     * Invoice with the individual tickets bought directly for the user. Tickets owned
     * by group tickets are not individual purchases and are not listed.
     */
    public Invoice forUser(User user) {
        InvoiceBuilder builder = builders.get()
                .recipient("USER", user.getEmail(), user.getName() + " " + user.getSurname());
        builder.section("Individual tickets");
        for (Ticket t : tickets.getTickets()) {
            if (t instanceof IndividualTicket it && it.getUser().getDni().equals(user.getDni())) {
                builder.item(it.getId() + " (" + it.getDate() + ")", it.getPrice());
            }
        }
        return builder.build();
    }

    /**
     * Invoice with the group tickets bought for the group, without their constituents.
     */
    public Invoice forGroup(Group group) {
        InvoiceBuilder builder = builders.get().recipient("GROUP", group.getId(), group.getName());
        builder.section("Group tickets");
        for (Ticket t : tickets.getTickets()) {
            if (t instanceof GroupTicket gt && gt.getGroup().getId().equals(group.getId())) {
                builder.item(gt.getId() + " (" + gt.getDate() + ", " + gt.getTickets().size() + " members)",
                        gt.getPrice());
            }
        }
        return builder.build();
    }
}
