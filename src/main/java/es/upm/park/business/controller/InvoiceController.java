package es.upm.park.business.controller;

import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.invoice.GroupInvoiceBuilder;
import es.upm.park.business.invoice.Invoice;
import es.upm.park.business.invoice.UserInvoiceBuilder;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;

/**
 * Directs the invoice builders with the economic information stored in the
 * park. Invoices are produced on demand and not stored. Single instance.
 */
public final class InvoiceController {

    private Park park;
    private PassController passes;

    private InvoiceController() {
    }

    private static final class Holder {
        private static final InvoiceController INSTANCE = new InvoiceController();
    }

    public static InvoiceController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    private Park park() {
        if (park == null) {
            throw new IllegalStateException("InvoiceController has not been configured");
        }
        return park;
    }

    /**
     * Tickets bought directly for the user plus the user's passes on any of
     * their individual tickets. Group-ticket components are not charged.
     */
    public Invoice userInvoice(String email) throws ParkException {
        User user = park().findUserByEmail(email);
        if (user == null) {
            throw new NotFoundException("No active user with email " + email);
        }
        UserInvoiceBuilder builder = new UserInvoiceBuilder();
        builder.start(park().getCurrentDate());
        builder.forUser(user);
        for (Ticket ticket : park().tickets()) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                builder.addIndividualTicket(individual);
            }
        }
        for (Pass pass : passes.passesOf(park().individualTicketsOf(user))) {
            if (pass instanceof SpeedPass speedPass) {
                builder.addSpeedPass(speedPass);
            } else if (pass instanceof SpecialPass specialPass) {
                builder.addSpecialPass(specialPass);
            }
        }
        return builder.build();
    }

    /**
     * Group tickets bought by the group; members' passes are personal and
     * not included.
     */
    public Invoice groupInvoice(String groupId) throws ParkException {
        Group group = park().findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("No group with identifier " + groupId);
        }
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder();
        builder.start(park().getCurrentDate());
        builder.forGroup(group);
        for (Ticket ticket : park().tickets()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                builder.addGroupTicket(groupTicket);
            }
        }
        return builder.build();
    }
}
