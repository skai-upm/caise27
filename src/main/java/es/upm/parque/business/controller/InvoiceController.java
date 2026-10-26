package es.upm.parque.business.controller;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceBuilder;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.Values;
import java.util.List;
import java.util.function.Supplier;

/**
 * Director of invoice construction: decides which sections apply to each recipient type and feeds
 * them to an {@link InvoiceBuilder} step by step.
 */
public class InvoiceController {

    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final Supplier<InvoiceBuilder> builderFactory;

    public InvoiceController(UserController userController, GroupController groupController,
                             TicketController ticketController, Supplier<InvoiceBuilder> builderFactory) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.builderFactory = builderFactory;
    }

    /**
     * Invoice of a user: the individual tickets bought directly for them (internal tickets of group
     * tickets are billed to the group and not repeated).
     */
    public Invoice forUser(String email) {
        User user = userController.findByEmail(email);
        if (user == null) {
            throw new ParkException("No user with email " + email);
        }
        InvoiceBuilder builder = builderFactory.get()
                .recipient(user.getName() + " " + user.getSurname() + " (" + user.getEmail() + ")");
        List<IndividualTicket> tickets = ticketController.getStandaloneTicketsOf(user);
        if (!tickets.isEmpty()) {
            builder.section("Individual tickets");
            for (IndividualTicket t : tickets) {
                builder.item("Ticket " + t.getId() + " " + Values.formatDate(t.getDate()), t.getPrice());
            }
        }
        return builder.build();
    }

    /**
     * Invoice of a group: its group ticket purchases with their totals.
     */
    public Invoice forGroup(String groupId) {
        Group group = groupController.findById(groupId);
        if (group == null) {
            throw new ParkException("No group with identifier " + groupId);
        }
        InvoiceBuilder builder = builderFactory.get().recipient("Group " + group.getId() + " - " + group.getName());
        List<GroupTicket> tickets = ticketController.getGroupTicketsOf(group);
        if (!tickets.isEmpty()) {
            builder.section("Group tickets");
            for (GroupTicket t : tickets) {
                builder.item("Group ticket " + t.getId() + " " + Values.formatDate(t.getDate()) + " ("
                        + t.getTickets().size() + " members)", t.getPrice());
            }
        }
        return builder.build();
    }
}
