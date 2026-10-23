package es.upm.etsisi.park.business.controller;

import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.park.business.model.invoice.Invoice;
import es.upm.etsisi.park.business.model.invoice.UserInvoiceBuilder;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.user.User;

/**
 * Generates invoices on demand from the current park state. It directs the
 * invoice builders, invoking only the steps that apply to the recipient and
 * have data. Singleton; invoices are not persisted.
 */
public final class InvoiceController {

    public static final String USER = "USER";
    public static final String GROUP = "GROUP";

    private static final InvoiceController INSTANCE = new InvoiceController();

    private SystemController system;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(SystemController system, UserController users, GroupController groups,
                          TicketController tickets) {
        this.system = system;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
    }

    /** Invoice for {@code USER} (key = email) or {@code GROUP} (key = group identifier). */
    public Invoice create(String recipientType, String key) throws ParkException {
        String type = recipientType == null ? "" : recipientType.trim().toUpperCase();
        return switch (type) {
            case USER -> userInvoice(key);
            case GROUP -> groupInvoice(key);
            default -> throw new ValidationException("Invalid invoice type '" + recipientType + "'. Valid types: "
                    + USER + ", " + GROUP);
        };
    }

    public Invoice userInvoice(String email) throws ParkException {
        User user = users.require(email);
        UserInvoiceBuilder builder = new UserInvoiceBuilder();
        builder.header("INVOICE - USER", system.getDate());
        builder.customer(user);

        List<IndividualTicket> direct = tickets.directTicketsOf(user);
        if (!direct.isEmpty()) {
            builder.directTickets(direct);
        }
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (IndividualTicket ticket : tickets.individualTicketsOf(user)) {
            speedPasses.addAll(ticket.getSpeedPasses());
            if (ticket.hasSpecialPass()) {
                specialPasses.add(ticket.getSpecialPass());
            }
        }
        if (!speedPasses.isEmpty()) {
            builder.speedPasses(speedPasses);
        }
        if (!specialPasses.isEmpty()) {
            builder.specialPasses(specialPasses);
        }
        return builder.build();
    }

    public Invoice groupInvoice(String groupId) throws ParkException {
        Group group = groups.require(groupId);
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder();
        builder.header("INVOICE - GROUP", system.getDate());
        builder.group(group);
        List<GroupTicket> groupTickets = tickets.groupTicketsOf(group);
        if (!groupTickets.isEmpty()) {
            builder.groupTickets(groupTickets);
        }
        return builder.build();
    }
}
