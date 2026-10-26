package es.upm.parque.business.controller;

import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceBuilder;
import es.upm.parque.business.invoice.InvoiceDirector;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.User;

/**
 * Generates invoices on demand for users and groups. Invoices are never stored.
 */
public class InvoiceController {
    private static final InvoiceController INSTANCE = new InvoiceController();

    private UserController userController;
    private GroupController groupController;
    private TicketController ticketController;
    private SystemDate systemDate;
    private InvoiceBuilder builder;
    private InvoiceDirector director;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the collaborators used to gather invoice data and the builder and director used to assemble it.
     */
    public InvoiceController configure(UserController userController, GroupController groupController,
            TicketController ticketController, SystemDate systemDate, InvoiceBuilder builder,
            InvoiceDirector director) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.systemDate = systemDate;
        this.builder = builder;
        this.director = director;
        return this;
    }

    public Invoice userInvoice(String email) {
        User user = userController.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        return director.constructUserInvoice(builder, user, ticketController.directTicketsOf(user), systemDate.get());
    }

    public Invoice groupInvoice(String groupId) {
        Group group = groupController.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("group " + groupId + " does not exist");
        }
        return director.constructGroupInvoice(builder, group, ticketController.groupTicketsOf(group),
                systemDate.get());
    }
}
