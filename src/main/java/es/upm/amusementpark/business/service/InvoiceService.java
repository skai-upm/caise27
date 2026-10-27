package es.upm.amusementpark.business.service;

import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.business.exception.InvoiceOperationException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

/**
 * Generates invoices on demand from the park's tickets, directing the invoice
 * builders.
 */
public class InvoiceService {

    private static final InvoiceService INSTANCE = new InvoiceService();

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private ParkController park;

    private InvoiceService() {
    }

    public static InvoiceService getInstance() {
        return INSTANCE;
    }

    /** Injects the collaborators of this controller; called during application initialization. */
    public void configure(UserRepository users, GroupRepository groups, TicketRepository tickets, ParkController park) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.park = park;
    }

    /** Invoice of an active user: the tickets bought directly for the user. */
    public Invoice createUserInvoice(String email) throws InvoiceOperationException {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new InvoiceOperationException("No active user with email " + email + " exists."));
        UserInvoiceBuilder builder = new UserInvoiceBuilder().forUser(user);
        for (IndividualTicket ticket : tickets.findIndividualTicketsOf(user)) {
            if (!ticket.isPartOfGroupTicket()) {
                builder.addIndividualTicket(ticket);
            }
        }
        return builder.build();
    }

    /** Invoice of a group: the group tickets it purchased. */
    public Invoice createGroupInvoice(String groupId) throws InvoiceOperationException {
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new InvoiceOperationException("No group with identifier " + groupId + " exists."));
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder().forGroup(group);
        for (GroupTicket ticket : park.groupTicketsOf(group)) {
            builder.addGroupTicket(ticket);
        }
        return builder.build();
    }
}
