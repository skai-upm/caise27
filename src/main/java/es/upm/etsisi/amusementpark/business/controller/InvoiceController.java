package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.SpeedPass;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates invoices on demand from the park state. A single instance exists;
 * its collaborator, the park controller, is injected at start-up.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private ParkController park;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController parkController) {
        this.park = parkController;
    }

    /** Builds the invoice for {@code USER;<email>} or {@code GROUP;<identifier>}. */
    public Invoice create(String recipientType, String recipient) throws ParkException {
        return switch (recipientType) {
            case "USER" -> userInvoice(recipient);
            case "GROUP" -> groupInvoice(recipient);
            default -> throw new InvalidDataException("unknown invoice type " + recipientType
                    + "; expected USER or GROUP");
        };
    }

    public Invoice userInvoice(String email) throws ParkException {
        User user = park.requireUser(email);
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (Pass pass : park.passesOf(user)) {
            if (pass instanceof SpeedPass speedPass) {
                speedPasses.add(speedPass);
            } else if (pass instanceof SpecialPass specialPass) {
                specialPasses.add(specialPass);
            }
        }
        return new UserInvoiceBuilder()
                .forUser(user)
                .individualTickets(park.directTicketsOf(user))
                .speedPasses(speedPasses)
                .specialPasses(specialPasses)
                .build();
    }

    public Invoice groupInvoice(String identifier) throws ParkException {
        Group group = park.requireGroup(identifier);
        return new GroupInvoiceBuilder()
                .forGroup(group)
                .groupTickets(park.groupTicketsOf(group))
                .build();
    }
}
