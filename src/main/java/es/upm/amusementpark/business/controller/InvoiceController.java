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
 * known to the park, using a fresh builder from the injected supplier for
 * each invoice. One instance per execution.
 */
public final class InvoiceController {

    static final String INDIVIDUAL_TICKETS = "Individual tickets";
    static final String GROUP_TICKETS = "Group tickets";

    private static InvoiceController instance;

    private final UserController users;
    private final ParkController park;
    private final Supplier<InvoiceBuilder> builders;

    private InvoiceController(UserController users, ParkController park, Supplier<InvoiceBuilder> builders) {
        this.users = users;
        this.park = park;
        this.builders = builders;
    }

    /** Creates the execution's instance with its collaborators. */
    public static synchronized InvoiceController initialize(UserController users, ParkController park,
                                                            Supplier<InvoiceBuilder> builders) {
        if (instance != null) {
            throw new IllegalStateException("InvoiceController is already initialized");
        }
        instance = new InvoiceController(users, park, builders);
        return instance;
    }

    public static synchronized InvoiceController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("InvoiceController is not initialized");
        }
        return instance;
    }

    /** Ends the execution's instance so a new execution can initialize its own. */
    public static synchronized void release() {
        instance = null;
    }

    /** Invoice of the tickets bought directly for a user; group-ticket constituents are not counted. */
    public Invoice userInvoice(String email) {
        User user = users.findByEmail(email);
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
