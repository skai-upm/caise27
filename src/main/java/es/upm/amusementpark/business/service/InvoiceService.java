package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Director of invoice construction: inspects the system state and calls only the builder steps
 * that apply to the recipient.
 */
public class InvoiceService {

    private UserService users;
    private GroupService groups;
    private TicketService tickets;
    private PassService passes;

    private static final InvoiceService INSTANCE = new InvoiceService();

    private InvoiceService() {
    }

    public static InvoiceService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies of this controller; called during application initialization.
     */
    public InvoiceService configure(UserService users, GroupService groups, TicketService tickets, PassService passes) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
        return this;
    }

    public Invoice userInvoice(String email) throws NotFoundException {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        UserInvoiceBuilder builder = new UserInvoiceBuilder().user(user);
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                builder.directTicket(individual);
            }
        }
        for (Pass pass : passes.findAll()) {
            if (pass.getTicket().getUser() == user) {
                if (pass instanceof SpeedPass speed) {
                    builder.speedPass(speed);
                } else if (pass instanceof SpecialPass special) {
                    builder.specialPass(special);
                }
            }
        }
        return builder.build();
    }

    public Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = groups.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found"));
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder().group(group);
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                builder.groupTicket(groupTicket);
            }
        }
        return builder.build();
    }
}
