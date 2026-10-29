package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Single invoice director: gathers the economic information of a user or group and drives the
 * corresponding builder. Invoices are generated on demand and not stored.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private UserController userController;
    private ParkController parkController;
    private PassController passController;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(UserController users, ParkController park, PassController passes) {
        this.userController = users;
        this.parkController = park;
        this.passController = passes;
    }

    public Invoice userInvoice(String email) throws ParkException {
        User user = userController.requireByEmail(email);
        List<IndividualTicket> tickets = new ArrayList<>();
        for (Ticket ticket : parkController.getTickets()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                tickets.add(individual);
            }
        }
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (Pass pass : passController.getPasses()) {
            if (pass.getTicket().getUser() != user) {
                continue;
            }
            if (pass instanceof SpeedPass speed) {
                speedPasses.add(speed);
            } else if (pass instanceof SpecialPass special) {
                specialPasses.add(special);
            }
        }
        return new UserInvoiceBuilder()
                .forUser(user)
                .withTickets(tickets)
                .withSpeedPasses(speedPasses)
                .withSpecialPasses(specialPasses)
                .build();
    }

    public Invoice groupInvoice(String groupId) throws ParkException {
        Group group = parkController.findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("group not found: " + groupId);
        }
        List<GroupTicket> groupTickets = new ArrayList<>();
        for (Ticket ticket : parkController.getTickets()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                groupTickets.add(gt);
            }
        }
        return new GroupInvoiceBuilder()
                .forGroup(group)
                .withGroupTickets(groupTickets)
                .build();
    }
}
