package es.upm.etsisi.amusementpark.business.controller;

import java.util.List;

import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * The single invoice controller. It directs the invoice builders with data obtained
 * from the park controller injected through {@link #configure(ParkController)}.
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

    public Invoice userInvoice(String email) throws NotFoundException {
        User user = park.requireUser(email);
        List<IndividualTicket> direct = park.getTickets().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .filter(t -> t.getUser() == user)
                .toList();
        List<Pass> personal = park.getPasses().stream().filter(p -> p.getTicket().getUser() == user).toList();
        return new UserInvoiceBuilder()
                .customer(user)
                .individualTickets(direct)
                .speedPasses(personal.stream().filter(SpeedPass.class::isInstance).toList())
                .specialPasses(personal.stream().filter(SpecialPass.class::isInstance).toList())
                .build();
    }

    public Invoice groupInvoice(String groupIdentifier) throws NotFoundException {
        Group group = park.requireGroup(groupIdentifier);
        return new GroupInvoiceBuilder()
                .group(group)
                .groupTickets(park.groupTicketsOf(group))
                .build();
    }
}
