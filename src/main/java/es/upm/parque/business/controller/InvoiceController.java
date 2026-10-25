package es.upm.parque.business.controller;

import java.util.ArrayList;
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
 * Generates invoices on demand (Singleton). Invoices are not stored.
 */
public final class InvoiceController {

    public static final String USER = "USER";
    public static final String GROUP = "GROUP";

    private static InvoiceController instance;

    private ParkController park;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;
    private InvoiceDirector director;
    private Supplier<InvoiceBuilder> builderFactory;

    private InvoiceController() {
    }

    public static synchronized InvoiceController getInstance() {
        if (instance == null) {
            instance = new InvoiceController();
        }
        return instance;
    }

    public void configure(ParkController park, UserController users, GroupController groups,
            TicketController tickets, InvoiceDirector director, Supplier<InvoiceBuilder> builderFactory) {
        this.park = park;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.director = director;
        this.builderFactory = builderFactory;
    }

    /**
     * Builds the invoice of a USER (by email) or a GROUP (by identifier).
     */
    public String createInvoice(String recipientType, String recipient) {
        Invoice invoice;
        if (USER.equals(recipientType)) {
            invoice = userInvoice(recipient);
        } else if (GROUP.equals(recipientType)) {
            invoice = groupInvoice(recipient);
        } else {
            return "ERROR Unknown invoice recipient type: " + recipientType + " (expected USER or GROUP)";
        }
        if (invoice == null) {
            return "ERROR Unknown " + recipientType.toLowerCase() + ": " + recipient;
        }
        List<String> lines = new ArrayList<>();
        lines.add("OK INVOICE " + recipientType + " " + recipient);
        lines.addAll(invoice.render());
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * The invoice of a user, or {@code null} if no user has that email.
     */
    public Invoice userInvoice(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            return null;
        }
        List<Pass> passes = new ArrayList<>();
        for (IndividualTicket ticket : tickets.ticketsOf(user)) {
            passes.addAll(ticket.getPasses());
        }
        return director.buildUserInvoice(builderFactory.get(), user, park.getCurrentDate(),
                tickets.directTicketsOf(user), passes);
    }

    /**
     * The invoice of a group, or {@code null} if no group has that identifier.
     */
    public Invoice groupInvoice(String groupId) {
        Group group = groups.find(groupId);
        if (group == null) {
            return null;
        }
        return director.buildGroupInvoice(builderFactory.get(), group, park.getCurrentDate(),
                tickets.groupTicketsOf(group));
    }
}
