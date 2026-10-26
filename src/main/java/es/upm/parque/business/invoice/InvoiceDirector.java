package es.upm.parque.business.invoice;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Knows which steps each kind of invoice needs and runs them on a builder. Sections without data are skipped.
 */
public class InvoiceDirector {

    /**
     * @param directTickets tickets bought directly for the user
     * @param allTickets every individual ticket of the user (direct and inside group tickets), source of its passes
     */
    public Invoice constructUserInvoice(InvoiceBuilder builder, User user, List<IndividualTicket> directTickets,
            List<IndividualTicket> allTickets, LocalDate issueDate) {
        builder.reset();
        builder.buildHeader("USER", user.getDni(), user.getName() + " " + user.getSurname(), issueDate);
        if (!directTickets.isEmpty()) {
            List<InvoiceLine> lines = new ArrayList<>();
            for (IndividualTicket t : directTickets) {
                lines.add(new InvoiceLine(t.getId() + " " + t.getDate(), t.getPrice()));
            }
            builder.buildSection("Individual tickets", lines);
        }
        List<InvoiceLine> passLines = new ArrayList<>();
        for (IndividualTicket t : allTickets) {
            for (Pass pass : t.getPasses()) {
                passLines.add(new InvoiceLine(pass.getId() + " " + pass.getKind(), pass.getPrice()));
            }
        }
        if (!passLines.isEmpty()) {
            builder.buildSection("Passes", passLines);
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
