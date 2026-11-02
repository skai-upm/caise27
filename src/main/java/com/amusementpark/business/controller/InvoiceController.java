package com.amusementpark.business.controller;

import com.amusementpark.business.invoice.Invoice;
import com.amusementpark.business.invoice.InvoiceBuilder;
import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.User;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Invoice director: decides which builder steps apply to each recipient type.
 */
public class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private TicketController tickets;
    private Supplier<InvoiceBuilder> builders;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /** Injects the ticket controller and the factory of fresh builders used for each invoice. */
    public void configure(TicketController tickets, Supplier<InvoiceBuilder> builders) {
        this.tickets = tickets;
        this.builders = builders;
    }

    /** Standalone tickets bought for the user; constituents of group tickets are not repeated. */
    public Invoice userInvoice(User user, LocalDate date) {
        return builders.get()
                .start("User " + user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")", date)
                .addTickets(tickets.standaloneTicketsOf(user))
                .build();
    }

    /** Group tickets bought by the group, without their constituents. */
    public Invoice groupInvoice(Group group, LocalDate date) {
        return builders.get()
                .start("Group " + group.getId() + " " + group.getName(), date)
                .addGroupTickets(tickets.groupTicketsOf(group))
                .build();
    }
}
