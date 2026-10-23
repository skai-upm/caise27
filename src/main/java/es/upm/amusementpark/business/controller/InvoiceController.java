package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Director of invoice construction: gathers the economic data of a user or a
 * group from the other controllers and calls only the builder steps for which
 * there is data.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private SystemController system;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private PassController passes;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /** Injects the controllers that provide the invoiced data. */
    public void configure(SystemController system, UserController users, GroupController groups,
                          TicketController tickets, PassController passes) {
        this.system = system;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
    }

    public Invoice userInvoice(String email) throws NotFoundException {
        User user = users.require(email);
        List<IndividualTicket> bought = new ArrayList<>();
        for (Ticket ticket : tickets.getTickets()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                bought.add(individual);
            }
        }
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (Pass pass : passes.getPasses()) {
            if (pass.getTicket().getUser() != user) {
                continue;
            }
            if (pass instanceof SpeedPass speedPass) {
                speedPasses.add(speedPass);
            } else if (pass instanceof SpecialPass specialPass) {
                specialPasses.add(specialPass);
            }
        }

        UserInvoiceBuilder builder = new UserInvoiceBuilder().issuedOn(system.getDate()).customer(user);
        if (!bought.isEmpty()) {
            builder.individualTickets(bought);
        }
        if (!speedPasses.isEmpty()) {
            builder.speedPasses(speedPasses);
        }
        if (!specialPasses.isEmpty()) {
            builder.specialPasses(specialPasses);
        }
        return builder.build();
    }

    public Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = groups.require(groupId);
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder().issuedOn(system.getDate()).group(group);
        List<GroupTicket> purchases = tickets.groupTicketsOf(group);
        if (!purchases.isEmpty()) {
            builder.groupTickets(purchases);
        }
        return builder.build();
    }
}
