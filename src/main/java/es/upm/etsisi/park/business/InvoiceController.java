package es.upm.etsisi.park.business;

import es.upm.etsisi.park.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.InvoiceDirector;
import es.upm.etsisi.park.business.invoice.UserInvoiceBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates user and group invoices on demand from the current park state.
 */
public final class InvoiceController {

    private static final InvoiceController INSTANCE = new InvoiceController();

    private final InvoiceDirector director = new InvoiceDirector();
    private Park park;

    private InvoiceController() {
    }

    public static InvoiceController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    public Invoice userInvoice(String email) {
        User user = park.findUser(email);
        if (user == null) {
            throw new NotFoundException("Active user not found: " + email);
        }
        List<IndividualTicket> direct = new ArrayList<>();
        List<Pass> personalPasses = new ArrayList<>();
        for (Ticket t : park.getTickets()) {
            if (t instanceof IndividualTicket i && i.getUser() == user) {
                direct.add(i);
            }
        }
        for (Pass p : park.getPasses()) {
            if (p.getTicket().getUser() == user) {
                personalPasses.add(p);
            }
        }
        return director.construct(new UserInvoiceBuilder(user, direct, personalPasses));
    }

    public Invoice groupInvoice(String groupId) {
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new NotFoundException("Group not found: " + groupId);
        }
        List<GroupTicket> purchases = new ArrayList<>();
        for (Ticket t : park.getTickets()) {
            if (t instanceof GroupTicket g && g.getGroup() == group) {
                purchases.add(g);
            }
        }
        return director.construct(new GroupInvoiceBuilder(group, purchases));
    }
}
