package es.upm.parque.business.invoice;

import java.util.List;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

/**
 * Knows which building steps apply to each kind of recipient.
 */
public class InvoiceDirector {

    static final String INDIVIDUAL_TICKETS = "Individual tickets";
    static final String PERSONAL_PASSES = "Personal passes";
    static final String GROUP_TICKETS = "Group tickets";

    /**
     * User invoice: tickets bought directly for the user and the passes of all the user's tickets.
     */
    public Invoice userInvoice(InvoiceBuilder builder, User user, List<IndividualTicket> directTickets,
                               List<Pass> passes) {
        return builder.start("USER " + user.getEmail() + " (" + user.getName() + " " + user.getSurname() + ")")
                .addSection(INDIVIDUAL_TICKETS, directTickets.stream()
                        .map(t -> new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice()))
                        .toList())
                .addSection(PERSONAL_PASSES, passes.stream()
                        .map(p -> new InvoiceLine(p.getId() + " " + p.getKind() + " (ticket " + p.getTicket().getId()
                                + ")", p.getPrice()))
                        .toList())
                .build();
    }

    /**
     * Group invoice: the group's group-ticket purchases only.
     */
    public Invoice groupInvoice(InvoiceBuilder builder, Group group, List<GroupTicket> groupTickets) {
        return builder.start("GROUP " + group.getId() + " (" + group.getName() + ")")
                .addSection(GROUP_TICKETS, groupTickets.stream()
                        .map(t -> new InvoiceLine(t.getId() + " " + t.getDate() + " x" + t.getTickets().size(),
                                t.getPrice()))
                        .toList())
                .build();
    }
}
