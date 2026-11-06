package es.upm.park.business.controller;

import es.upm.park.business.invoice.GroupInvoiceBuilder;
import es.upm.park.business.invoice.Invoice;
import es.upm.park.business.invoice.UserInvoiceBuilder;
import es.upm.park.business.model.Group;

/**
 * Invoicing controller (single instance per execution). It directs the invoice
 * builders using the park state of the {@link ParkController} injected at
 * application initialization.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private ParkController park;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    /** Injects the park management collaborator. */
    public void setParkController(ParkController park) {
        this.park = park;
    }

    /** Invoice of an active user's direct tickets and personal passes. */
    public Invoice userInvoice(String email) {
        ParkController source = requirePark();
        return new UserInvoiceBuilder()
                .forUser(email, source.userDni(email))
                .directTickets(source.ticketsOf(email))
                .speedPasses(source.passesOf(email))
                .specialPasses(source.passesOf(email))
                .build();
    }

    /** Invoice of the group tickets bought by an existing group. */
    public Invoice groupInvoice(String groupId) {
        ParkController source = requirePark();
        Group group = source.requireGroup(groupId);
        return new GroupInvoiceBuilder()
                .forGroup(group)
                .groupTickets(source.groupTicketsOf(group))
                .build();
    }

    private ParkController requirePark() {
        if (park == null) {
            throw new IllegalStateException("InvoiceController is not configured with a ParkController");
        }
        return park;
    }
}
