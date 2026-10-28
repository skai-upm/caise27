package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Director of invoice construction: inspects the system state and calls only the builder steps
 * that apply to the recipient.
 */
public class InvoiceService {

    private final UserService users;
    private final GroupService groups;
    private final TicketService tickets;

    public InvoiceService(UserService users, GroupService groups, TicketService tickets) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
    }

    public Invoice userInvoice(String email) throws NotFoundException {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        UserInvoiceBuilder builder = new UserInvoiceBuilder().user(user);
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                builder.directTicket(individual);
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
