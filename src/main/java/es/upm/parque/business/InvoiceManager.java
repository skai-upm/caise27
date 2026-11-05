package es.upm.parque.business;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.User;

/**
 * Director of the invoice Builder: resolves the recipient and runs the construction steps in a fixed order.
 * Invoices are generated on demand from the current state and are not stored.
 */
public class InvoiceManager {

    private final ParkController park;

    public InvoiceManager(ParkController park) {
        this.park = park;
    }

    public Invoice forUser(String email) throws NotFoundException {
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        return construct(new UserInvoiceBuilder(user, park));
    }

    public Invoice forGroup(String id) throws NotFoundException {
        Group group = park.getGroups().findById(id);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + id);
        }
        return construct(new GroupInvoiceBuilder(group, park));
    }

    static Invoice construct(InvoiceBuilder builder) {
        builder.buildRecipient();
        builder.buildPurchases();
        builder.buildPersonalPasses();
        return builder.getResult();
    }
}
