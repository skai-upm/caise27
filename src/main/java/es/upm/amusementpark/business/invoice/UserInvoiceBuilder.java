package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.ParkFormat;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice of a user: tickets bought directly for them plus their SpeedPasses and Special Passes (including passes
 * on tickets that compose a group ticket, whose own price is invoiced to the group).
 */
public class UserInvoiceBuilder extends AbstractInvoiceBuilder {

    private final User user;
    private final List<IndividualTicket> directTickets;
    private final List<SpeedPass> speedPasses;
    private final List<SpecialPass> specialPasses;

    public UserInvoiceBuilder(LocalDate issueDate, User user, List<IndividualTicket> directTickets,
                              List<SpeedPass> speedPasses, List<SpecialPass> specialPasses) {
        super(issueDate);
        this.user = user;
        this.directTickets = List.copyOf(directTickets);
        this.speedPasses = List.copyOf(speedPasses);
        this.specialPasses = List.copyOf(specialPasses);
    }

    @Override
    public void buildHeader() {
        setTitle("USER INVOICE");
    }

    @Override
    public void buildRecipient() {
        addRecipientLine("Name: " + user.getFirstName() + " " + user.getSurnames());
        addRecipientLine("DNI: " + user.getDni());
        addRecipientLine("Email: " + user.getEmail());
    }

    @Override
    public void buildItems() {
        List<InvoiceLine> tickets = new ArrayList<>();
        for (IndividualTicket ticket : directTickets) {
            tickets.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ParkFormat.formatDate(ticket.getDate())
                    + ")", ticket.getPrice()));
        }
        addSection("Individual tickets", tickets);

        List<InvoiceLine> speed = new ArrayList<>();
        for (SpeedPass pass : speedPasses) {
            speed.add(new InvoiceLine("SpeedPass " + pass.getId() + " for ticket " + pass.getTicket().getId()
                    + (pass.isConsumed() ? " (consumed)" : ""), pass.getPrice()));
        }
        addSection("SpeedPasses", speed);

        List<InvoiceLine> special = new ArrayList<>();
        for (SpecialPass pass : specialPasses) {
            special.add(new InvoiceLine("Special Pass " + pass.getId() + " for ticket " + pass.getTicket().getId()
                    + (pass.isConsumed() ? " (consumed)" : ""), pass.getPrice()));
        }
        addSection("Special Passes", special);
    }
}
