package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;

/**
 * Single invoice controller acting as the Builder director: gathers the economic information from
 * the injected controllers and drives the user or group invoice builder.
 */
public final class InvoiceController {

    public static final String USER = "USER";
    public static final String GROUP = "GROUP";

    private static final InvoiceController INSTANCE = new InvoiceController();

    private ParkController park;
    private UserController users;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController parkController, UserController userController) {
        this.park = parkController;
        this.users = userController;
    }

    public Invoice create(String recipientType, String recipient) {
        return switch (recipientType) {
            case USER -> userInvoice(recipient);
            case GROUP -> groupInvoice(recipient);
            default -> throw new ValidationException("invoice type must be USER or GROUP: " + recipientType);
        };
    }

    public Invoice userInvoice(String email) {
        User user = users.requireByEmail(email);
        UserInvoiceBuilder builder = new UserInvoiceBuilder().reset().forUser(user);
        for (IndividualTicket ticket : park.directTicketsOf(user)) {
            builder.ticket(ticket);
        }
        for (IndividualTicket ticket : park.individualTicketsOf(user)) {
            for (SpeedPass pass : ticket.getSpeedPasses()) {
                builder.speedPass(pass);
            }
            ticket.getSpecialPass().ifPresent(builder::specialPass);
        }
        return builder.build();
    }

    public Invoice groupInvoice(String groupId) {
        Group group = park.requireGroup(groupId);
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder().reset().forGroup(group);
        for (GroupTicket ticket : park.groupTicketsOf(group)) {
            builder.groupTicket(ticket);
        }
        return builder.build();
    }
}
