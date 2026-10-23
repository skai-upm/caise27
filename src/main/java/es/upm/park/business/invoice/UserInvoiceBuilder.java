package es.upm.park.business.invoice;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;

/**
 * Builds a user's invoice: the user's identity, tickets bought directly for
 * the user, and the user's SpeedPasses and Special Passes.
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    static final String TICKETS = "Individual tickets";
    static final String SPEED_PASSES = "SpeedPasses";
    static final String SPECIAL_PASSES = "Special Passes";

    public UserInvoiceBuilder() {
        super("USER INVOICE");
    }

    public UserInvoiceBuilder forUser(User user) {
        addRecipientLine("Name: " + user.getFullName());
        addRecipientLine("DNI: " + user.getDni());
        addRecipientLine("Email: " + user.getEmail());
        return this;
    }

    public UserInvoiceBuilder addIndividualTicket(IndividualTicket ticket) {
        addItem(TICKETS, new InvoiceItem("Ticket " + ticket.getId() + " for " + ticket.getDate(), ticket.getPrice()));
        return this;
    }

    public UserInvoiceBuilder addSpeedPass(SpeedPass pass) {
        addItem(SPEED_PASSES, new InvoiceItem(passDescription("SpeedPass", pass.getId(), pass.getTicket(),
                pass.isConsumed()), pass.getPrice()));
        return this;
    }

    public UserInvoiceBuilder addSpecialPass(SpecialPass pass) {
        addItem(SPECIAL_PASSES, new InvoiceItem(passDescription("Special Pass", pass.getId(), pass.getTicket(),
                pass.isConsumed()), pass.getPrice()));
        return this;
    }

    private static String passDescription(String kind, String id, IndividualTicket ticket, boolean consumed) {
        return kind + " " + id + " (ticket " + ticket.getId() + ", " + ticket.getDate()
                + (consumed ? ", consumed" : "") + ")";
    }
}
