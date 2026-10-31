package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Director that drives an {@link InvoiceBuilder} through the steps of a user
 * or group invoice. Ticket data is passed in by the caller.
 */
public class InvoiceService {

    static final String USER = "USER";
    static final String GROUP = "GROUP";

    private final Supplier<InvoiceBuilder> builders;

    public InvoiceService(Supplier<InvoiceBuilder> builders) {
        this.builders = builders;
    }

    /**
     * Invoice for the tickets bought directly for the user and the personal
     * passes of every ticket the user holds. Constituents of group tickets are
     * only used for their passes, so they are never counted twice.
     */
    public Invoice userInvoice(User user, List<IndividualTicket> directTickets, List<IndividualTicket> heldTickets) {
        List<InvoiceLine> ticketLines = new ArrayList<>();
        for (IndividualTicket ticket : directTickets) {
            ticketLines.add(new InvoiceLine("Ticket " + ticket.getId() + " (" + ticket.getDate() + ")",
                    ticket.getPrice()));
        }
        List<InvoiceLine> passLines = new ArrayList<>();
        for (IndividualTicket ticket : heldTickets) {
            for (Pass pass : ticket.getPasses()) {
                passLines.add(new InvoiceLine(pass.getKind() + " " + pass.getId() + " (ticket " + ticket.getId() + ")",
                        pass.getPrice()));
            }
        }
        return builders.get()
                .recipient(USER, user.getEmail(), user.getName() + " " + user.getSurname())
                .section("Individual tickets", ticketLines)
                .section("Personal passes", passLines)
                .build();
    }

    /** Invoice for the group-ticket purchases of the group only. */
    public Invoice groupInvoice(Group group, List<GroupTicket> groupTickets) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (GroupTicket ticket : groupTickets) {
            lines.add(new InvoiceLine("Group ticket " + ticket.getId() + " (" + ticket.getDate() + ", "
                    + ticket.getTickets().size() + " members)", ticket.getPrice()));
        }
        return builders.get()
                .recipient(GROUP, group.getId(), group.getName())
                .section("Group tickets", lines)
                .build();
    }
}
