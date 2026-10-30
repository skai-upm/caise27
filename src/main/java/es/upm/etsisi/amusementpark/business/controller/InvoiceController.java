package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.List;

/**
 * Invoice generation (Singleton). Acts as the director of the invoice builders, feeding them the
 * economic information found in the repositories.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private ParkController park;
    private PassController passes;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(ParkController park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    /**
     * INVOICE CREATE;USER|GROUP;id
     */
    public Invoice create(String recipientType, String id) {
        return switch (recipientType.trim()) {
            case "USER" -> userInvoice(id);
            case "GROUP" -> groupInvoice(id);
            default -> throw new ValidationException("Invoice type must be USER or GROUP: " + recipientType);
        };
    }

    public Invoice userInvoice(String email) {
        User user = park.users().findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        List<IndividualTicket> direct = park.tickets().findAll().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .filter(t -> t.getUser() == user)
                .toList();
        List<Pass> userPasses = park.tickets().findAll().stream()
                .flatMap(t -> t.getIndividualTickets().stream())
                .filter(t -> t.getUser() == user)
                .flatMap(t -> passes.passesOf(t).stream())
                .toList();
        return new UserInvoiceBuilder()
                .forUser(user)
                .withTickets(direct)
                .withSpeedPasses(only(userPasses, SpeedPass.class))
                .withSpecialPasses(only(userPasses, SpecialPass.class))
                .build();
    }

    public Invoice groupInvoice(String groupId) {
        Group group = park.groups().findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
        return new GroupInvoiceBuilder()
                .forGroup(group)
                .withGroupTickets(park.tickets().findByGroup(group))
                .build();
    }

    private static <P extends Pass> List<P> only(List<Pass> passes, Class<P> kind) {
        return passes.stream().filter(kind::isInstance).map(kind::cast).toList();
    }
}
