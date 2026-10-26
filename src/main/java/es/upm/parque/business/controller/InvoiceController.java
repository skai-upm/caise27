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
    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final SystemDate systemDate;
    private final InvoiceBuilder builder;
    private final InvoiceDirector director;

    public InvoiceController(UserController userController, GroupController groupController,
            TicketController ticketController, SystemDate systemDate, InvoiceBuilder builder,
            InvoiceDirector director) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.systemDate = systemDate;
        this.builder = builder;
        this.director = director;
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
