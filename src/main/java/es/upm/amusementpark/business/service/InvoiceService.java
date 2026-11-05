package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.util.List;

/**
 * Generates invoices on demand, directing the invoice builders.
 */
public class InvoiceService {

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private PassRepository passes = new PassRepository();

    private InvoiceService() {
    }

    private static final class Holder {
        private static final InvoiceService INSTANCE = new InvoiceService();
    }

    /** The single instance of this controller. */
    public static InvoiceService getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the pass repository used for the passes section of user invoices. */
    public InvoiceService injectPasses(PassRepository passes) {
        this.passes = passes;
        return this;
    }

    /** Injects the repositories this controller works with (done during application initialization). */
    public InvoiceService inject(UserRepository users, GroupRepository groups, TicketRepository tickets) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        return this;
    }


    public Invoice create(String recipientType, String identifier) {
        return switch (recipientType) {
            case "USER" -> userInvoice(identifier);
            case "GROUP" -> groupInvoice(identifier);
            default -> throw new InvalidDataException("invalid invoice type '" + recipientType + "', expected USER or GROUP");
        };
    }

    public Invoice userInvoice(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("user not found"));
        List<IndividualTicket> direct = tickets.individualTicketsOf(user).stream()
                .filter(t -> tickets.containerOf(t).isEmpty()).toList();
        List<es.upm.amusementpark.business.model.Pass> userPasses = tickets.individualTicketsOf(user).stream()
                .flatMap(t -> passes.passesOf(t).stream()).toList();
        return new UserInvoiceBuilder().forUser(user).directTickets(direct).passes(userPasses).build();
    }

    public Invoice groupInvoice(String groupIdentifier) {
        Group group = groups.findById(groupIdentifier)
                .orElseThrow(() -> new NotFoundException("group '" + groupIdentifier + "' not found"));
        return new GroupInvoiceBuilder().forGroup(group).groupTickets(tickets.groupTicketsOf(group)).build();
    }
}
