package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds user invoices: identification, directly bought individual tickets and personal passes.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    public UserInvoiceBuilder() {
        super("USER INVOICE");
    }

    public UserInvoiceBuilder forUser(User user) {
        addHeaderLine("User: " + user.getFirstName() + " " + user.getSurnames());
        addHeaderLine("DNI: " + user.getDni());
        addHeaderLine("Email: " + user.getEmail());
        return this;
    }

    /**
     * Individual tickets bought directly for the user (never composition tickets of group tickets).
     */
    public UserInvoiceBuilder withTickets(List<IndividualTicket> tickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            lines.add(new InvoiceLine("Ticket " + t.getId() + " (" + t.getDate() + ")", t.getPrice()));
        }
        addSection("Individual tickets", lines);
        return this;
    }

    public UserInvoiceBuilder withSpeedPasses(List<SpeedPass> passes) {
        addSection("SpeedPasses", passLines("SpeedPass", passes));
        return this;
    }

    public UserInvoiceBuilder withSpecialPasses(List<SpecialPass> passes) {
        addSection("Special Passes", passLines("Special Pass", passes));
        return this;
    }

    private static List<InvoiceLine> passLines(String kind, List<? extends Pass> passes) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(new InvoiceLine(kind + " " + p.getId() + " for ticket " + p.getTicket().getId()
                    + (p.isConsumed() ? " (consumed)" : ""), p.getPrice()));
        }
        return lines;
    }
}
