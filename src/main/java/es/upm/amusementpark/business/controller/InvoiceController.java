package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.amusementpark.business.invoice.InvoiceDirector;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * On-demand invoices for users and groups (Singleton), built from the stored purchases with the Builder pattern.
 */
public final class InvoiceController {

    private static InvoiceController instance;

    private Park park;
    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private PassRepository passes;
    private final InvoiceDirector director = new InvoiceDirector();

    private InvoiceController() {
    }

    public static synchronized InvoiceController getInstance() {
        if (instance == null) {
            instance = new InvoiceController();
        }
        return instance;
    }

    public void configure(Park park, UserRepository users, GroupRepository groups, TicketRepository tickets,
                          PassRepository passes) {
        this.park = park;
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
    }

    public Invoice create(String recipientType, String key) {
        return switch (recipientType) {
            case "USER" -> userInvoice(key);
            case "GROUP" -> groupInvoice(key);
            default -> throw new InvalidDataException("invalid invoice type " + recipientType
                    + " (allowed: USER, GROUP)");
        };
    }

    public Invoice userInvoice(String email) {
        User user = users.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no active user with email " + email);
        }
        List<IndividualTicket> direct = new ArrayList<>();
        for (Ticket ticket : tickets.findAll()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                direct.add(individual);
            }
        }
        List<SpeedPass> speed = new ArrayList<>();
        List<SpecialPass> special = new ArrayList<>();
        for (Pass pass : passes.findAll()) {
            if (pass.getTicket().getUser() == user) {
                if (pass instanceof SpeedPass speedPass) {
                    speed.add(speedPass);
                } else if (pass instanceof SpecialPass specialPass) {
                    special.add(specialPass);
                }
            }
        }
        return build(new UserInvoiceBuilder(park.getDate(), user, direct, speed, special));
    }

    public Invoice groupInvoice(String groupId) {
        Group group = groups.findById(groupId);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + groupId);
        }
        return build(new GroupInvoiceBuilder(park.getDate(), group, tickets.groupTicketsOf(group)));
    }

    private Invoice build(InvoiceBuilder builder) {
        return director.construct(builder);
    }
}
