package es.upm.parque.business;

import java.util.Comparator;
import java.util.List;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.InvoiceItem;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Builds the invoice of a user: identification and the individual tickets bought directly for the user.
 * Tickets that are part of a group ticket are excluded, because the group ticket already includes their price.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final ParkController park;

    public UserInvoiceBuilder(User user, ParkController park) {
        super("Invoice for user " + user.getEmail());
        this.user = user;
        this.park = park;
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addRecipientLine("DNI: " + user.getDni());
        addRecipientLine("Email: " + user.getEmail());
    }

    @Override
    public void buildPurchases() {
        List<InvoiceItem> items = park.getTickets().individualTicketsOf(user).stream()
                .filter(t -> !park.isPartOfGroupTicket(t))
                .sorted(Comparator.comparing(Ticket::getDate).thenComparing(Ticket::getId))
                .map(UserInvoiceBuilder::item)
                .toList();
        addSection("Individual tickets", items);
    }

    private static InvoiceItem item(IndividualTicket ticket) {
        return new InvoiceItem("Ticket " + ticket.getId() + " for " + ticket.getDate(), ticket.getPrice(), List.of());
    }
}
