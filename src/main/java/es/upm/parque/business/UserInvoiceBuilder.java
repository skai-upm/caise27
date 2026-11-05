package es.upm.parque.business;

import java.util.Comparator;
import java.util.List;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.InvoiceItem;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Builds the invoice of a user: identification and the individual tickets bought directly for the user.
 * Tickets that are part of a group ticket are excluded, because the group ticket already includes their price;
 * the passes of all the user's tickets (group components included) are personal purchases and are listed.
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

    /** Passes are personal purchases: those of every ticket the user holds, group components included. */
    @Override
    public void buildPersonalPasses() {
        List<Pass> passes = park.getPasses().findAll().stream().filter(p -> p.getTicket().belongsTo(user)).toList();
        addSection("SpeedPasses", passes.stream().filter(SpeedPass.class::isInstance).map(UserInvoiceBuilder::item)
                .toList());
        addSection("Special Passes", passes.stream().filter(SpecialPass.class::isInstance)
                .map(UserInvoiceBuilder::item).toList());
    }

    private static InvoiceItem item(Pass pass) {
        String description = pass.typeName() + " " + pass.getId() + " for ticket " + pass.getTicket().getId()
                + (pass.isConsumed() ? " (consumed)" : "");
        return new InvoiceItem(description, pass.getPrice(), List.of());
    }

    private static InvoiceItem item(IndividualTicket ticket) {
        return new InvoiceItem("Ticket " + ticket.getId() + " for " + ticket.getDate(), ticket.getPrice(), List.of());
    }
}
