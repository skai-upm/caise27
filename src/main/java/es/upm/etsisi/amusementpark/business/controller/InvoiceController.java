package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.List;

/**
 * Single controller generating invoices on demand from the park state. Its
 * collaboration with the park controller is injected at initialization.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private final InvoiceDirector director = new InvoiceDirector();
    private ParkController park;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /** Injects the park controller whose state is invoiced. */
    public void configure(ParkController park) {
        this.park = park;
    }

    /** Invoice of an active user with the tickets bought directly for them. */
    public Invoice createUserInvoice(String email) {
        ParkController state = park();
        User user = state.findUser(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        List<IndividualTicket> direct = state.getTickets().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .filter(ticket -> ticket.getUser() == user)
                .toList();
        return director.construct(new UserInvoiceBuilder(user, direct));
    }

    /** Invoice of an existing group with its group tickets. */
    public Invoice createGroupInvoice(String identifier) {
        ParkController state = park();
        Group group = state.findGroup(identifier)
                .orElseThrow(() -> new NotFoundException("Group not found: " + identifier));
        return director.construct(new GroupInvoiceBuilder(group, state.findGroupTickets(group)));
    }

    private ParkController park() {
        if (park == null) {
            throw new OperationNotAllowedException("Invoice controller is not configured");
        }
        return park;
    }
}
