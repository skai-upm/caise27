package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;

/**
 * Builds the invoice of a user: identification and the user's personal
 * purchases.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    static final String TICKETS = "Individual tickets";
    static final String SPEED_PASSES = "SpeedPasses";
    static final String SPECIAL_PASSES = "Special Passes";

    public UserInvoiceBuilder() {
        super("INVOICE - USER");
    }

    public UserInvoiceBuilder forUser(User user) {
        addHeaderLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addHeaderLine("DNI: " + user.getDni());
        addHeaderLine("Email: " + user.getEmail());
        return this;
    }

    /** Adds a ticket bought directly for the user. */
    public UserInvoiceBuilder addIndividualTicket(IndividualTicket ticket) {
        addLine(TICKETS, InvoiceLine.charged(ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                ticket.getPrice()));
        return this;
    }

    /** Adds a SpeedPass bought for one of the user's tickets. */
    public UserInvoiceBuilder addSpeedPass(SpeedPass pass) {
        addLine(SPEED_PASSES, passLine(pass));
        return this;
    }

    /** Adds a Special Pass bought for one of the user's tickets. */
    public UserInvoiceBuilder addSpecialPass(SpecialPass pass) {
        addLine(SPECIAL_PASSES, passLine(pass));
        return this;
    }

    private static InvoiceLine passLine(Pass pass) {
        return InvoiceLine.charged(pass.getIdentifier() + " (ticket " + pass.getTicket().getIdentifier() + ")",
                pass.getPrice());
    }
}
