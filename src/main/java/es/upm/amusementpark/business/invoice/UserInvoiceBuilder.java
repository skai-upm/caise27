package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/** Builds invoices for a single user. */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public UserInvoiceBuilder() {
        super("USER INVOICE");
    }

    public UserInvoiceBuilder addUser(User user) {
        addSection("User", List.of(InvoiceLine.detail("Email: " + user.getEmail()),
                InvoiceLine.detail("DNI: " + user.getDni())));
        return this;
    }

    /** Individual tickets purchased directly for the user (not group-ticket components). */
    public UserInvoiceBuilder addIndividualTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(InvoiceLine.item("Ticket " + ticket.getIdentifier() + " (" + ticket.getDate() + ")",
                    ticket.getPrice()));
        }
        addSection("Individual tickets", lines);
        return this;
    }

    /** SpeedPasses associated with the user's individual tickets (personal purchases). */
    public UserInvoiceBuilder addSpeedPasses(List<SpeedPass> passes) {
        addSection("SpeedPasses", passLines("SpeedPass", passes));
        return this;
    }

    /** Special Passes associated with the user's individual tickets (personal purchases). */
    public UserInvoiceBuilder addSpecialPasses(List<SpecialPass> passes) {
        addSection("Special Passes", passLines("Special Pass", passes));
        return this;
    }

    private static List<InvoiceLine> passLines(String kind, List<? extends Pass> passes) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(InvoiceLine.item(kind + " " + pass.getIdentifier() + " (ticket "
                    + pass.getTicket().getIdentifier() + (pass.isConsumed() ? ", consumed" : "") + ")",
                    pass.getPrice()));
        }
        return lines;
    }
}
