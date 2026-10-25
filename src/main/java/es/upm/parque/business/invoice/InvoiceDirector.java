package es.upm.parque.business.invoice;

import java.time.LocalDate;
import java.util.List;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

/**
 * Decides which builder steps apply to each kind of recipient.
 */
public class InvoiceDirector {

    /**
     * A user invoice: tickets bought directly by the user and the passes of all the user's tickets.
     */
    public Invoice buildUserInvoice(InvoiceBuilder builder, User user, LocalDate date,
            List<IndividualTicket> directTickets, List<Pass> passes) {
        builder.reset();
        builder.buildHeader("USER", user.getName() + " " + user.getSurname() + " <" + user.getEmail() + ">", date);
        builder.buildIndividualTickets(directTickets);
        builder.buildPasses(passes);
        return builder.getResult();
    }

    /**
     * A group invoice: only the group's group tickets.
     */
    public Invoice buildGroupInvoice(InvoiceBuilder builder, Group group, LocalDate date,
            List<GroupTicket> groupTickets) {
        builder.reset();
        builder.buildHeader("GROUP", group.getId() + " " + group.getName(), date);
        builder.buildGroupTickets(groupTickets);
        return builder.getResult();
    }
}
