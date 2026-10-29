package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;

/**
 * Builds user invoices: identifying data, directly purchased individual tickets, SpeedPasses and
 * Special Passes.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    static final String TICKETS = "Individual tickets";
    static final String SPEED_PASSES = "SpeedPasses";
    static final String SPECIAL_PASSES = "Special Passes";

    @Override
    public UserInvoiceBuilder reset() {
        super.reset();
        return this;
    }

    public UserInvoiceBuilder forUser(User user) {
        setTitle("INVOICE - USER");
        addHeaderLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addHeaderLine("DNI: " + user.getDni());
        addHeaderLine("Email: " + user.getEmail());
        return this;
    }

    public UserInvoiceBuilder ticket(IndividualTicket ticket) {
        addItem(TICKETS, new InvoiceItem("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")",
                ticket.getPrice()));
        return this;
    }

    public UserInvoiceBuilder speedPass(SpeedPass pass) {
        addItem(SPEED_PASSES, new InvoiceItem("SpeedPass " + pass.getId() + " for ticket "
                + pass.getTicket().getId() + consumedLabel(pass.isConsumed()), pass.getPrice()));
        return this;
    }

    public UserInvoiceBuilder specialPass(SpecialPass pass) {
        addItem(SPECIAL_PASSES, new InvoiceItem("Special Pass " + pass.getId() + " for ticket "
                + pass.getTicket().getId() + consumedLabel(pass.isConsumed()), pass.getPrice()));
        return this;
    }

    private static String consumedLabel(boolean consumed) {
        return consumed ? " [consumed]" : "";
    }
}
