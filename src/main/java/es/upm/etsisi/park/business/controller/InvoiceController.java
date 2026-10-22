package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.PassRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Directs invoice construction from the stored park state. Single instance per execution.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();
    private static final String NAME = "InvoiceController";

    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private PassRepository passes;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(UserRepository users, GroupRepository groups, TicketRepository tickets, PassRepository passes) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
    }

    /** {@code recipientType} is USER (key = email) or GROUP (key = group id). */
    public Invoice create(String recipientType, String key) {
        Checks.configured(users, NAME);
        return switch (recipientType.toUpperCase(Locale.ROOT)) {
            case "USER" -> forUser(key);
            case "GROUP" -> forGroup(key);
            default -> throw new ValidationException("Unknown invoice type: " + recipientType + " (allowed: USER, GROUP)");
        };
    }

    public Invoice forUser(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (IndividualTicket ticket : tickets.findAllIndividualByUser(user)) {
            for (Pass pass : passes.findByTicket(ticket)) {
                if (pass instanceof SpeedPass speed) {
                    speedPasses.add(speed);
                } else if (pass instanceof SpecialPass special) {
                    specialPasses.add(special);
                }
            }
        }
        return new UserInvoiceBuilder()
                .forUser(user)
                .withIndividualTickets(tickets.findStandaloneByUser(user))
                .withSpeedPasses(speedPasses)
                .withSpecialPasses(specialPasses)
                .build();
    }

    public Invoice forGroup(String groupId) {
        Group group = groups.findById(groupId).orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
        return new GroupInvoiceBuilder()
                .forGroup(group)
                .withGroupTickets(tickets.findByGroup(group))
                .build();
    }
}
