package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceDirector;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates user and group invoices on demand from the current park state.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private final InvoiceDirector director = new InvoiceDirector();
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private PassController passes;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void setCollaborators(UserController users, GroupController groups, TicketController tickets,
                                 PassController passes) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
    }

    public Invoice userInvoice(String email) throws NotFoundException {
        User user = users.requireUser(email);
        List<Pass> userPasses = new ArrayList<>();
        for (IndividualTicket ticket : tickets.ticketsOf(user)) {
            userPasses.addAll(passes.passesOf(ticket));
        }
        return director.construct(new UserInvoiceBuilder(user, tickets.standaloneTicketsOf(user), userPasses));
    }

    public Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = groups.requireGroup(groupId);
        return director.construct(new GroupInvoiceBuilder(group, tickets.groupTicketsOf(group)));
    }
}
