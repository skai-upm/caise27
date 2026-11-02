package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.SpecialPass;
import es.upm.etsisi.park.business.SpeedPass;
import es.upm.etsisi.park.business.User;
import es.upm.etsisi.park.business.ValueParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user invoice: directly purchased tickets plus personal passes (also those on
 * group-ticket constituents, whose ticket price is already invoiced to the group).
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    private final User user;
    private final List<IndividualTicket> directTickets;
    private final List<Pass> passes;

    public UserInvoiceBuilder(User user, List<IndividualTicket> directTickets, List<Pass> passes) {
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
        this.passes = List.copyOf(passes);
    }

    @Override
    public void buildHeader() {
        setTitle("USER INVOICE");
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Customer: " + user.getFirstName() + " " + user.getSurnames());
        addRecipientLine("DNI: " + user.getDni());
        addRecipientLine("Email: " + user.getEmail());
    }

    @Override
    public void buildPurchases() {
        List<InvoiceLine> tickets = new ArrayList<>();
        for (IndividualTicket t : directTickets) {
            tickets.add(new ChargeLine("Ticket " + t.getId() + " (" + ValueParser.formatDate(t.getDate()) + ")", t.getPrice()));
        }
        addSection("Tickets", tickets);
        addSection("SpeedPasses", passLines(SpeedPass.class, "SpeedPass "));
        addSection("Special Passes", passLines(SpecialPass.class, "Special Pass "));
    }

    private List<InvoiceLine> passLines(Class<? extends Pass> kind, String label) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass p : passes) {
            if (kind.isInstance(p)) {
                String state = p.isConsumed() ? ", consumed" : "";
                lines.add(new ChargeLine(label + p.getId() + " for ticket " + p.getTicket().getId() + state, p.getPrice()));
            }
        }
        return lines;
    }
}
