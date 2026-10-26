package es.upm.parque.business.invoice;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Knows which steps each kind of invoice needs and runs them on a builder. Sections without data are skipped.
 */
public class InvoiceDirector {

    public Invoice constructUserInvoice(InvoiceBuilder builder, User user, List<IndividualTicket> directTickets,
            LocalDate issueDate) {
        builder.reset();
        builder.buildHeader("USER", user.getDni(), user.getName() + " " + user.getSurname(), issueDate);
        if (!directTickets.isEmpty()) {
            List<InvoiceLine> lines = new ArrayList<>();
            for (IndividualTicket t : directTickets) {
                lines.add(new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice()));
            }
            builder.buildSection("Individual tickets", lines);
        }
        return builder.getResult();
    }

    public Invoice constructGroupInvoice(InvoiceBuilder builder, Group group, List<GroupTicket> groupTickets,
            LocalDate issueDate) {
        builder.reset();
        builder.buildHeader("GROUP", group.getId(), group.getName(), issueDate);
        if (!groupTickets.isEmpty()) {
            List<InvoiceLine> lines = new ArrayList<>();
            for (GroupTicket t : groupTickets) {
                lines.add(new InvoiceLine(t.getId() + " " + t.getDate() + " x" + t.getTickets().size(), t.getPrice()));
            }
            builder.buildSection("Group tickets", lines);
        }
        return builder.getResult();
    }
}
