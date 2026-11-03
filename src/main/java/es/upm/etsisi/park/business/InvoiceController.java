package es.upm.etsisi.park.business;

import es.upm.etsisi.park.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.UserInvoiceBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates invoices on demand from the current park state, directing the
 * invoice builders. Invoices are not persisted.
 */
public final class InvoiceController {

    public static final String USER = "USER";
    public static final String GROUP = "GROUP";

    private static final InvoiceController INSTANCE = new InvoiceController();

    private Park park;
    private PassController passes;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    public Invoice create(String type, String target) throws ParkException {
        return switch (type) {
            case USER -> userInvoice(target);
            case GROUP -> groupInvoice(target);
            default -> throw new ValidationException("invalid invoice type " + type + ": expected USER or GROUP");
        };
    }

    Invoice userInvoice(String email) throws NotFoundException {
        User user = park.findUser(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        List<IndividualTicket> direct = new ArrayList<>();
        for (Ticket t : park.getTickets()) {
            if (t instanceof IndividualTicket i && i.getUser() == user) {
                direct.add(i);
            }
        }
        List<SpeedPass> speed = new ArrayList<>();
        List<SpecialPass> special = new ArrayList<>();
        for (IndividualTicket t : park.allIndividualTickets()) {
            if (t.getUser() != user) {
                continue;
            }
            for (Pass p : passes.passesOf(t)) {
                if (p instanceof SpeedPass s) {
                    speed.add(s);
                } else if (p instanceof SpecialPass s) {
                    special.add(s);
                }
            }
        }
        return new UserInvoiceBuilder().forUser(user).directTickets(direct).speedPasses(speed)
                .specialPasses(special).build();
    }

    Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + groupId);
        }
        return new GroupInvoiceBuilder().forGroup(group).groupTickets(park.groupTicketsOf(group)).build();
    }
}
