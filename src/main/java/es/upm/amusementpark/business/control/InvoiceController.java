package es.upm.amusementpark.business.control;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceDirector;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Invoice generation. Only one instance exists per execution; its
 * collaborators are injected at application initialization.
 */
public final class InvoiceController {

    private ParkController park;
    private InvoiceDirector director;

    private InvoiceController() {
    }

    private static final class Holder {
        private static final InvoiceController INSTANCE = new InvoiceController();
    }

    public static InvoiceController getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management and the invoice director. */
    public void configure(ParkController parkController, InvoiceDirector invoiceDirector) {
        this.park = Objects.requireNonNull(parkController);
        this.director = Objects.requireNonNull(invoiceDirector);
    }

    /** Invoice of an active user: only tickets purchased directly for that user. */
    public Invoice createUserInvoice(String email) {
        requireConfigured();
        User user = park.existingUser(email);
        List<IndividualTicket> direct = new ArrayList<>();
        for (Ticket ticket : park.getTickets()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser().equals(user)) {
                direct.add(individual);
            }
        }
        return director.userInvoice(user, direct);
    }

    /** Invoice of an existing group: the group tickets it purchased. */
    public Invoice createGroupInvoice(String groupIdentifier) {
        requireConfigured();
        Group group = park.existingGroup(groupIdentifier);
        return director.groupInvoice(group, park.groupTicketsOf(group));
    }

    private void requireConfigured() {
        if (park == null || director == null) {
            throw new IllegalStateException("invoice controller used before initialization");
        }
    }
}
