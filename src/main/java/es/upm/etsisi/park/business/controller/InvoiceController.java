package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.SpecialPass;
import es.upm.etsisi.park.business.model.SpeedPass;
import es.upm.etsisi.park.business.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Singleton acting as director of the invoice builders: it selects the data that applies to the
 * invoiced party and calls only the matching construction steps.
 */
public final class InvoiceController {

    public static final String USER = "USER";
    public static final String GROUP = "GROUP";

    private static InvoiceController instance;

    private SystemController system;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;

    private InvoiceController() {
    }

    public static synchronized InvoiceController getInstance() {
        if (instance == null) {
            instance = new InvoiceController();
        }
        return instance;
    }

    public void initialize(SystemController systemController, UserController userController,
                           GroupController groupController, TicketController ticketController) {
        this.system = systemController;
        this.users = userController;
        this.groups = groupController;
        this.tickets = ticketController;
    }

    public Invoice create(String recipientType, String identifier) {
        String type = recipientType == null ? "" : recipientType.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case USER -> userInvoice(users.requireByEmail(identifier));
            case GROUP -> groupInvoice(groups.require(identifier));
            default -> throw new ValidationException("Invalid invoice type: " + recipientType + ". Expected USER or GROUP");
        };
    }

    public Invoice userInvoice(User user) {
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (IndividualTicket ticket : tickets.ticketsHeldBy(user)) {
            speedPasses.addAll(ticket.getSpeedPasses());
            ticket.getSpecialPass().ifPresent(specialPasses::add);
        }
        UserInvoiceBuilder builder = new UserInvoiceBuilder();
        builder.issuedOn(system.getCurrentDate());
        return builder.forUser(user)
                .withIndividualTickets(tickets.directTicketsOf(user))
                .withSpeedPasses(speedPasses)
                .withSpecialPasses(specialPasses)
                .build();
    }

    public Invoice groupInvoice(Group group) {
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder();
        builder.issuedOn(system.getCurrentDate());
        return builder.forGroup(group)
                .withGroupTickets(tickets.groupTicketsOf(group))
                .build();
    }
}
