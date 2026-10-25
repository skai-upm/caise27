package es.upm.parque.business.controller;

import java.util.List;
import java.util.function.Supplier;

import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceBuilder;
import es.upm.parque.business.invoice.InvoiceDirector;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

/**
 * Generates invoices on demand from the recorded purchases. Singleton.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private final InvoiceDirector director = new InvoiceDirector();
    private Supplier<InvoiceBuilder> builders;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /**
     * @param builders source of a fresh builder for each invoice
     */
    public void configure(Supplier<InvoiceBuilder> builders, UserController users, GroupController groups,
                          TicketController tickets) {
        this.builders = builders;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
    }

    public Invoice userInvoice(String email) {
        User user = users.get(email);
        List<IndividualTicket> owned = tickets.ticketsOf(user);
        List<IndividualTicket> direct = owned.stream().filter(t -> !t.isPartOfGroupTicket()).toList();
        List<Pass> passes = owned.stream().flatMap(t -> t.getPasses().stream()).toList();
        return director.userInvoice(builders.get(), user, direct, passes);
    }

    public Invoice groupInvoice(String groupId) {
        Group group = groups.get(groupId);
        return director.groupInvoice(builders.get(), group, tickets.groupTicketsOf(group));
    }
}
