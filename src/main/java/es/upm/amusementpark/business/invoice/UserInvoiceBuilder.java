package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.pass.SpeedPass;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a user's invoice: identity, tickets bought directly for the user, and the user's passes
 * (including passes on group-ticket composition tickets, whose ticket price is not charged here).
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    public static final String TICKETS = "Individual tickets";
    public static final String SPEED_PASSES = "SpeedPasses";
    public static final String SPECIAL_PASSES = "Special Passes";

    private final User user;
    private final List<IndividualTicket> directTickets;
    private final List<Pass> passes;

    public UserInvoiceBuilder(User user, List<IndividualTicket> directTickets, List<Pass> passes) {
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
        this.passes = List.copyOf(passes);
        reset();
    }

    @Override
    public void buildHeader() {
        setTitle("INVOICE - USER");
        addHeaderLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addHeaderLine("DNI: " + user.getDni());
        addHeaderLine("Email: " + user.getEmail());
    }

    @Override
    public void buildTicketItems() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket ticket : directTickets) {
            lines.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")", ticket.getPrice()));
        }
        addSection(TICKETS, lines);
    }

    @Override
    public void buildPassItems() {
        List<InvoiceLine> speed = new ArrayList<>();
        List<InvoiceLine> special = new ArrayList<>();
        for (Pass pass : passes) {
            InvoiceLine line = new InvoiceLine(pass.getKind() + " " + pass.getId() + " for ticket "
                    + pass.getTicket().getId() + (pass.isConsumed() ? " [consumed]" : ""), pass.getPrice());
            if (pass instanceof SpeedPass) {
                speed.add(line);
            } else if (pass instanceof SpecialPass) {
                special.add(line);
            }
        }
        addSection(SPEED_PASSES, speed);
        addSection(SPECIAL_PASSES, special);
    }
}
