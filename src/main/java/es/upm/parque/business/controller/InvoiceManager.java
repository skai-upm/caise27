package es.upm.parque.business.controller;

import java.util.Locale;
import java.util.function.Supplier;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.InvoiceBuilder;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.User;

/**
 * Generates invoices on demand, directing an {@link InvoiceBuilder} through the steps that apply
 * to each kind of recipient.
 */
public class InvoiceManager {

    public static final String INVALID_TYPE = "ERROR INVALID INVOICE TYPE";
    static final String USER = "USER";
    static final String GROUP = "GROUP";

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final SystemDate systemDate;
    private final Supplier<InvoiceBuilder> builderFactory;

    public InvoiceManager(UserManager users, GroupManager groups, TicketManager tickets, SystemDate systemDate,
                          Supplier<InvoiceBuilder> builderFactory) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.systemDate = systemDate;
        this.builderFactory = builderFactory;
    }

    public String create(String type, String recipient) {
        return switch (type.toUpperCase(Locale.ROOT)) {
            case USER -> userInvoice(recipient);
            case GROUP -> groupInvoice(recipient);
            default -> INVALID_TYPE;
        };
    }

    private String userInvoice(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            return GroupManager.USER_NOT_FOUND;
        }
        InvoiceBuilder builder = builderFactory.get()
                .recipient(USER, user.getEmail(), user.getName() + " " + user.getSurname())
                .issuedOn(systemDate.get())
                .section("Individual tickets");
        for (IndividualTicket ticket : tickets.ticketsHeldBy(user)) {
            builder.line(ticket.getId() + " " + SystemDate.format(ticket.getDate()), ticket.getPrice());
        }
        return builder.build().toText();
    }

    private String groupInvoice(String groupId) {
        Group group = groups.find(groupId);
        if (group == null) {
            return GroupManager.GROUP_NOT_FOUND;
        }
        InvoiceBuilder builder = builderFactory.get()
                .recipient(GROUP, group.getId(), group.getName())
                .issuedOn(systemDate.get())
                .section("Group tickets");
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            builder.line(ticket.getId() + " " + SystemDate.format(ticket.getDate()) + " (" + ticket.getTickets().size()
                    + " members, " + ticket.getDiscountPercent() + "% discount)", ticket.getPrice());
        }
        return builder.build().toText();
    }
}
