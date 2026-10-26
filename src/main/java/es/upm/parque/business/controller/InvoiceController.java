package es.upm.parque.business.controller;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceBuilder;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Directs an {@link InvoiceBuilder} to produce user and group invoices on demand.
 */
public class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private UserController userController;
    private GroupController groupController;
    private TicketController ticketController;
    private PassController passController;
    private InvoiceBuilder builder;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators of the controller and (re)loads its data. Called during application
     * initialization.
     */
    public InvoiceController configure(UserController userController, GroupController groupController,
                                       TicketController ticketController, PassController passController,
                                       InvoiceBuilder builder) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.passController = passController;
        this.builder = builder;
        return this;
    }

    /**
     * Invoice of the individual tickets bought directly for the user (tickets of group purchases excluded) and of
     * the personal passes of all the user's individual tickets.
     */
    public Invoice userInvoice(String email) {
        requireConfigured();
        User user = userController.findByEmail(email);
        if (user == null) {
            throw new BusinessException("No user with email " + email);
        }
        builder.startInvoice("USER", user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")");
        builder.startSection("Individual tickets");
        for (Ticket ticket : ticketController.getTickets()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user
                    && ticketController.findOwner(individual) == null) {
                builder.addLine(individual.getId() + " " + SystemDate.format(individual.getDate()), individual.getPrice());
            }
        }
        builder.startSection("Personal passes");
        for (Pass pass : passController.getPasses()) {
            if (pass.getTicket().getUser() == user) {
                builder.addLine(pass.getId() + " " + pass.getType() + " (" + pass.getTicket().getId() + ")",
                        pass.getPrice());
            }
        }
        return builder.build();
    }

    /**
     * Invoice of the group's group-ticket purchases, with their discounted amounts.
     */
    public Invoice groupInvoice(String groupId) {
        requireConfigured();
        Group group = groupController.find(groupId);
        if (group == null) {
            throw new BusinessException("No group with identifier " + groupId);
        }
        builder.startInvoice("GROUP", group.getName() + " (" + group.getId() + ")");
        builder.startSection("Group tickets");
        for (Ticket ticket : ticketController.getTickets()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                builder.addLine(groupTicket.getId() + " " + SystemDate.format(groupTicket.getDate()) + " "
                        + groupTicket.getTickets().size() + " members -" + groupTicket.getDiscountPercent() + "%",
                        groupTicket.getPrice());
            }
        }
        return builder.build();
    }

    private void requireConfigured() {
        if (builder == null) {
            throw new IllegalStateException("InvoiceController is not configured");
        }
    }
}
