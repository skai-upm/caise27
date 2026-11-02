package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.User;
import java.util.List;

/**
 * Singleton controller that directs the invoice builders over the current park state.
 */
public final class InvoiceController {

    private Park park;

    private InvoiceController() {
    }

    private static final class Holder {
        private static final InvoiceController INSTANCE = new InvoiceController();
    }

    public static InvoiceController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park parkState) {
        park = parkState;
    }

    /** Direct ticket purchases of the user plus every pass bought for any of their individual tickets. */
    public Invoice userInvoice(String email) throws NotFoundException {
        User user = park.requireUser(email);
        List<IndividualTicket> direct = park.tickets().stream()
                .filter(t -> t instanceof IndividualTicket it && it.getUser() == user)
                .map(IndividualTicket.class::cast).toList();
        List<Pass> own = park.passes().stream().filter(p -> p.getTicket().getUser() == user).toList();
        return new UserInvoiceBuilder().forUser(user)
                .withTickets(direct)
                .withSpeedPasses(own.stream().filter(SpeedPass.class::isInstance).map(SpeedPass.class::cast).toList())
                .withSpecialPasses(own.stream().filter(SpecialPass.class::isInstance).map(SpecialPass.class::cast)
                        .toList())
                .build();
    }

    /** Group tickets bought by the group; members' passes are personal and not included. */
    public Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = park.requireGroup(groupId);
        List<GroupTicket> bought = park.tickets().stream()
                .filter(t -> t instanceof GroupTicket gt && gt.getGroup() == group)
                .map(GroupTicket.class::cast).toList();
        return new GroupInvoiceBuilder().forGroup(group).withGroupTickets(bought).build();
    }
}
