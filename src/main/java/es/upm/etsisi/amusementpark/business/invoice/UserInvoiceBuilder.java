package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/** Builds a user's invoice from the tickets bought directly for that user and their personal passes. */
public class UserInvoiceBuilder extends InvoiceBuilder {

    private final User user;
    private final List<IndividualTicket> tickets;
    private final List<Pass> passes;

    /**
     * @param tickets tickets bought with TICKET CREATE for this user; tickets generated
     *                by a group ticket must not be passed here
     * @param passes  the user's personal passes, on any of their tickets
     */
    public UserInvoiceBuilder(User user, List<IndividualTicket> tickets, List<Pass> passes) {
        this.user = user;
        this.tickets = List.copyOf(tickets);
        this.passes = List.copyOf(passes);
    }

    @Override
    public void buildHeader() {
        setTitle("Invoice for user " + user.getName() + " " + user.getSurname() + " <" + user.getEmail()
                + "> (DNI " + user.getDni() + ")");
    }

    @Override
    public void buildTicketSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            lines.add(new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice()));
        }
        addSection("Individual tickets", lines);
    }

    @Override
    public void buildPassSection() {
        List<InvoiceLine> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(new InvoiceLine(p.getId() + " " + p.kind() + " for " + p.getTicket().getId(), p.getPrice()));
        }
        addSection("Personal passes", lines);
    }
}
