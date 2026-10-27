package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Director of invoice construction: decides which builder steps apply to each recipient.
 */
public class InvoiceController {

    private final ParkController park;
    private final InvoiceBuilder builder;

    public InvoiceController(ParkController park, InvoiceBuilder builder) {
        this.park = park;
        this.builder = builder;
    }

    /** Invoice of the individual tickets bought directly for an active user. */
    public Invoice userInvoice(String email) throws NotFoundException {
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        builder.forRecipient(Invoice.Type.USER)
                .recipient("Name", user.getFirstName() + " " + user.getSurnames())
                .recipient("DNI", user.getDni())
                .recipient("Email", user.getEmail())
                .section("Individual tickets");
        for (Ticket ticket : park.getTickets().findAll()) {
            if (ticket instanceof IndividualTicket it && it.involves(user)) {
                builder.item(it.getId() + " (" + it.getDate() + ")", it.getPrice());
            }
        }
        return builder.build();
    }

    /** Invoice of the group tickets bought by a group, with their composition as detail. */
    public Invoice groupInvoice(String groupId) throws NotFoundException {
        Group group = park.getGroups().findById(groupId);
        if (group == null) {
            throw new NotFoundException("Group not found");
        }
        builder.forRecipient(Invoice.Type.GROUP)
                .recipient("Group", group.getId())
                .recipient("Name", group.getName())
                .section("Group tickets");
        for (Ticket ticket : park.getTickets().findAll()) {
            if (ticket instanceof GroupTicket gt && gt.getGroup() == group) {
                builder.item(gt.getId() + " (" + gt.getDate() + ", " + gt.getComposition().size() + " members)",
                        gt.getPrice());
                for (IndividualTicket component : gt.getComposition()) {
                    builder.detail(component.getId() + " " + component.getUser().getEmail() + ": "
                            + component.getPrice().toPlainString() + " EUR");
                }
            }
        }
        return builder.build();
    }
}
